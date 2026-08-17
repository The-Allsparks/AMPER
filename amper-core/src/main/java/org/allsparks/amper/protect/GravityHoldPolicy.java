package org.allsparks.amper.protect;

/**
 * Gravity-loaded mechanism hold policy. AMPER never infers a safe holding
 * effort. The subsystem must declare a minimum absolute effort.
 */
public final class GravityHoldPolicy {
    private final double minimumAbsEffort;
    private final String mechanismId;

    private GravityHoldPolicy(String mechanismId, double minimumAbsEffort) {
        this.mechanismId = mechanismId;
        this.minimumAbsEffort = minimumAbsEffort;
    }

    /**
     * @param mechanismId non-empty name
     * @param minimumAbsEffort declared safe hold in {@code [0, 1]}
     */
    public static GravityHoldPolicy declare(String mechanismId, double minimumAbsEffort) {
        if (mechanismId == null || mechanismId.trim().isEmpty()) {
            throw new IllegalArgumentException("gravity-critical mechanismId is required");
        }
        if (Double.isNaN(minimumAbsEffort)
                || Double.isInfinite(minimumAbsEffort)
                || minimumAbsEffort < 0.0
                || minimumAbsEffort > 1.0) {
            throw new IllegalArgumentException("declared gravity hold must be finite in [0, 1]");
        }
        return new GravityHoldPolicy(mechanismId.trim(), minimumAbsEffort);
    }

    public String mechanismId() {
        return mechanismId;
    }

    public double minimumAbsEffort() {
        return minimumAbsEffort;
    }

    /** Enforce that an allowed effort does not drop below the declared hold. */
    public double enforce(double allowed, double requestedSign) {
        double min = minimumAbsEffort;
        double sign = requestedSign < 0 ? -1.0 : 1.0;
        if (Math.abs(allowed) < min) {
            return sign * min;
        }
        return allowed;
    }
}
