package org.allsparks.amper.api;

/**
 * Subsystem intent submitted to AMPER for optional constraint evaluation.
 *
 * <p>Effort is dimensionless in {@code [-1, 1]} unless a team documents a
 * different convention in {@link PowerPolicy}. Estimated current may be
 * {@link Double#NaN} when unknown.
 *
 * <p>This type is defined early for Phase 4 integration design. Phase 0–1 must
 * not use grants to change motor output.
 */
public final class PowerRequest {
    private final String source;
    private final double requestedEffort;
    private final double minimumUsefulEffort;
    private final PowerPriority priority;
    private final boolean safetyCritical;
    private final boolean gravityCritical;
    private final boolean interruptible;
    private final long maximumDelayNanos;
    private final double estimatedCurrentAmps;

    public PowerRequest(
            String source,
            double requestedEffort,
            double minimumUsefulEffort,
            PowerPriority priority,
            boolean safetyCritical,
            boolean gravityCritical,
            boolean interruptible,
            long maximumDelayNanos,
            double estimatedCurrentAmps) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("source must be non-empty");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority is required");
        }
        this.source = source;
        this.requestedEffort = requestedEffort;
        this.minimumUsefulEffort = minimumUsefulEffort;
        this.priority = priority;
        this.safetyCritical = safetyCritical;
        this.gravityCritical = gravityCritical;
        this.interruptible = interruptible;
        this.maximumDelayNanos = maximumDelayNanos;
        this.estimatedCurrentAmps = estimatedCurrentAmps;
    }

    public String source() {
        return source;
    }

    public double requestedEffort() {
        return requestedEffort;
    }

    public double minimumUsefulEffort() {
        return minimumUsefulEffort;
    }

    public PowerPriority priority() {
        return priority;
    }

    public boolean safetyCritical() {
        return safetyCritical;
    }

    public boolean gravityCritical() {
        return gravityCritical;
    }

    public boolean interruptible() {
        return interruptible;
    }

    public long maximumDelayNanos() {
        return maximumDelayNanos;
    }

    public double estimatedCurrentAmps() {
        return estimatedCurrentAmps;
    }
}
