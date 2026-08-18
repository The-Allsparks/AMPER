package org.allsparks.amper.protect;

import org.allsparks.amper.api.PowerLimitReason;

/**
 * Result of optional local protection. When {@link #constrained()} is true,
 * controllers should freeze or back-calculate integral terms (anti-windup).
 */
public final class ConstrainedCommand {
    private final double requested;
    private final double allowed;
    private final boolean constrained;
    private final PowerLimitReason reason;

    public ConstrainedCommand(double requested, double allowed, boolean constrained, PowerLimitReason reason) {
        this.requested = requested;
        this.allowed = allowed;
        this.constrained = constrained;
        this.reason = reason == null ? PowerLimitReason.NONE : reason;
    }

    public static ConstrainedCommand identity(double requested) {
        return new ConstrainedCommand(requested, requested, false, PowerLimitReason.FEATURE_DISABLED);
    }

    public double requested() {
        return requested;
    }

    public double allowed() {
        return allowed;
    }

    public boolean constrained() {
        return constrained;
    }

    public PowerLimitReason reason() {
        return reason;
    }
}
