package org.allsparks.amper.protect;

/**
 * Optional magnitude cap on commanded effort. Opt-in Phase 2 only.
 */
public final class CommandCap {
    private final double maxAbsEffort;

    public CommandCap(double maxAbsEffort) {
        if (Double.isNaN(maxAbsEffort) || Double.isInfinite(maxAbsEffort) || maxAbsEffort < 0.0) {
            throw new IllegalArgumentException("maxAbsEffort must be finite and nonnegative");
        }
        this.maxAbsEffort = maxAbsEffort;
    }

    public double apply(double requested) {
        if (Double.isNaN(requested)) {
            return requested;
        }
        if (requested > maxAbsEffort) {
            return maxAbsEffort;
        }
        if (requested < -maxAbsEffort) {
            return -maxAbsEffort;
        }
        return requested;
    }

    public double maxAbsEffort() {
        return maxAbsEffort;
    }
}
