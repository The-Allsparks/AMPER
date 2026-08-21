package org.allsparks.amper.protect;

import org.allsparks.amper.AmperFeatureFlags;
import org.allsparks.amper.api.PowerLimitReason;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Opt-in Phase 2 local protection. AMPER never wraps every FTC motor.
 *
 * <p><strong>Not student API.</strong> Experimental, default-off, and not
 * competition-ready until Control Hub evidence exists (issue #6). Do not
 * enable actuation in Phase 0/1.
 *
 * <p>Constraints apply only when <em>both</em> are true:
 * <ul>
 *   <li>this instance is {@link #enabled()}, and</li>
 *   <li>if session flags were supplied (via {@link #fromPolicy} or
 *       {@link Builder#sessionFlags}), {@link AmperFeatureFlags#isPhase2LocalProtection()}
 *       is true.</li>
 * </ul>
 *
 * <p>When either gate is off, {@link #apply(double, long)} returns the requested
 * command unchanged (subject only to ordinary floating-point representation of
 * the same {@code double}). Raw {@link #builder()} without session flags stays
 * an explicit local opt-in and is <strong>not</strong> session-gated — prefer
 * {@link #fromPolicy} or {@link org.allsparks.amper.AmperSession#localProtection(boolean)}
 * so the session flag remains a kill switch.
 *
 * <p>Experimental until robot characterization is complete. Defaults off.
 */
public final class LocalProtection {
    private final boolean enabled;
    private final AmperFeatureFlags sessionFlags;
    private final SlewRateLimiter slew;
    private final CommandCap cap;
    private final boolean capEnabled;
    private final GravityHoldPolicy gravity;
    private final boolean safetyCritical;
    private final InterruptibleLoadRecovery recovery;

    private LocalProtection(Builder builder) {
        this.enabled = builder.enabled;
        this.sessionFlags = builder.sessionFlags;
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

    /** True when session flags are present and Phase 2 is allowed. */
    public boolean sessionGateOpen() {
        return sessionFlags == null || sessionFlags.isPhase2LocalProtection();
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
        if (!enabled || !sessionGateOpen()) {
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
            throw new IllegalArgumentException("gravity-critical mechanisms must declare a safe minimum hold effort");
        }
    }

    /**
     * Builds protection from policy slew/cap settings and attaches the policy
     * feature flags as the session kill switch.
     */
    public static LocalProtection fromPolicy(PowerPolicy policy, boolean enabled) {
        Builder builder = builder().enabled(enabled);
        if (policy != null) {
            builder.sessionFlags(policy.featureFlags());
            builder.slew(new SlewRateLimiter(policy.slewMaxDeltaPerSecond()));
            if (policy.commandCapEnabled()) {
                builder.commandCap(new CommandCap(policy.commandCap()));
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private boolean enabled = false;
        private AmperFeatureFlags sessionFlags;
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

        /**
         * When set, {@link #apply} is identity unless
         * {@link AmperFeatureFlags#isPhase2LocalProtection()} is true.
         */
        public Builder sessionFlags(AmperFeatureFlags flags) {
            this.sessionFlags = flags;
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
