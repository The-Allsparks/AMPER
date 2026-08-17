package org.allsparks.amper.protect;

import org.allsparks.amper.api.PowerLimitReason;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Opt-in Phase 2 local protection. AMPER never wraps every FTC motor.
 *
 * <p>When {@code enabled} is false, {@link #apply(double, long)} returns the
 * requested command unchanged (subject only to ordinary floating-point
 * representation of the same {@code double}).
 *
 * <p>Experimental until robot characterization is complete. Defaults off.
 */
public final class LocalProtection {
    private final boolean enabled;
    private final SlewRateLimiter slew;
    private final CommandCap cap;
    private final boolean capEnabled;
    private final GravityHoldPolicy gravity;
    private final boolean safetyCritical;
    private final InterruptibleLoadRecovery recovery;

    private LocalProtection(Builder builder) {
        this.enabled = builder.enabled;
        this.slew = builder.slew;
        this.cap = builder.cap;
        this.capEnabled = builder.capEnabled;
        this.gravity = builder.gravity;
        this.safetyCritical = builder.safetyCritical;
        this.recovery = builder.recovery == null ? InterruptibleLoadRecovery.disabled() : builder.recovery;
    }

    public static LocalProtection disabled() {
        return builder().enabled(false).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean safetyCritical() {
        return safetyCritical;
    }

    public GravityHoldPolicy gravity() {
        return gravity;
    }

    public InterruptibleLoadRecovery recovery() {
        return recovery;
    }

    public ConstrainedCommand apply(double requested, long nowNanos) {
        if (!enabled) {
            return ConstrainedCommand.identity(requested);
        }
        double allowed = requested;
        PowerLimitReason reason = PowerLimitReason.NONE;
        if (slew != null) {
            double slewed = slew.apply(allowed, nowNanos);
            if (slewed != allowed) {
                reason = PowerLimitReason.LOCAL_SLEW_LIMIT;
            }
            allowed = slewed;
        }
        if (capEnabled && cap != null) {
            double capped = cap.apply(allowed);
            if (capped != allowed) {
                reason = PowerLimitReason.LOCAL_OUTPUT_CAP;
            }
            allowed = capped;
        }
        if (gravity != null) {
            double held = gravity.enforce(allowed, requested);
            if (held != allowed) {
                reason = PowerLimitReason.NONE;
            }
            allowed = held;
        }
        boolean constrained = allowed != requested;
        return new ConstrainedCommand(requested, allowed, constrained, constrained ? reason : PowerLimitReason.NONE);
    }

    /**
     * Reject incomplete gravity-critical configuration. Call from builders
     * before enabling Phase 2 on a lift/arm.
     */
    public static void requireGravityDeclaration(boolean gravityCritical, GravityHoldPolicy policy) {
        if (gravityCritical && policy == null) {
            throw new IllegalArgumentException(
                    "gravity-critical mechanisms must declare a safe minimum hold effort");
        }
    }

    public static LocalProtection fromPolicy(PowerPolicy policy, boolean enabled) {
        Builder builder = builder().enabled(enabled);
        if (policy != null) {
            builder.slew(new SlewRateLimiter(policy.slewMaxDeltaPerSecond()));
            if (policy.commandCapEnabled()) {
                builder.commandCap(new CommandCap(policy.commandCap()));
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private boolean enabled = false;
        private SlewRateLimiter slew;
        private CommandCap cap;
        private boolean capEnabled;
        private GravityHoldPolicy gravity;
        private boolean safetyCritical;
        private InterruptibleLoadRecovery recovery = InterruptibleLoadRecovery.disabled();

        public Builder enabled(boolean value) {
            this.enabled = value;
            return this;
        }

        public Builder slew(SlewRateLimiter slew) {
            this.slew = slew;
            return this;
        }

        public Builder commandCap(CommandCap cap) {
            this.cap = cap;
            this.capEnabled = cap != null;
            return this;
        }

        public Builder gravity(GravityHoldPolicy gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder gravityCritical(GravityHoldPolicy gravity) {
            if (gravity == null) {
                throw new IllegalArgumentException("gravity-critical configuration is incomplete");
            }
            this.gravity = gravity;
            return this;
        }

        public Builder safetyCritical(boolean value) {
            this.safetyCritical = value;
            return this;
        }

        public Builder recovery(InterruptibleLoadRecovery recovery) {
            this.recovery = recovery;
            return this;
        }

        public LocalProtection build() {
            requireGravityDeclaration(gravity != null && enabled, gravity);
            return new LocalProtection(this);
        }
    }
}
