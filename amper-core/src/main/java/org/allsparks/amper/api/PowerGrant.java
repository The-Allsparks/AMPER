package org.allsparks.amper.api;

/**
 * Constraint returned by AMPER for a submitted {@link PowerRequest}.
 *
 * <p>Until intervention phases are enabled and validated, callers must treat
 * grants as advisory telemetry only. {@link #confidence()} is in {@code [0, 1]}
 * and must never be presented as certainty.
 */
public final class PowerGrant {
    private final double allowedEffort;
    private final boolean delayed;
    private final PowerLimitReason reason;
    private final double confidence;

    public PowerGrant(double allowedEffort, boolean delayed, PowerLimitReason reason, double confidence) {
        if (reason == null) {
            throw new IllegalArgumentException("reason is required");
        }
        if (Double.isNaN(confidence) || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be in [0, 1]");
        }
        this.allowedEffort = allowedEffort;
        this.delayed = delayed;
        this.reason = reason;
        this.confidence = confidence;
    }

    /** Pass-through grant used when intervention features are disabled. */
    public static PowerGrant unrestricted(double requestedEffort) {
        return new PowerGrant(requestedEffort, false, PowerLimitReason.FEATURE_DISABLED, 1.0);
    }

    public double allowedEffort() {
        return allowedEffort;
    }

    public boolean delayed() {
        return delayed;
    }

    public PowerLimitReason reason() {
        return reason;
    }

    public double confidence() {
        return confidence;
    }
}
