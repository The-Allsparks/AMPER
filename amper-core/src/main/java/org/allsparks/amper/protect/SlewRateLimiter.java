package org.allsparks.amper.protect;

/**
 * Rate-limits effort changes. Experimental Phase 2 foundation. Disabled path
 * must not be used unless a subsystem opts in.
 */
public final class SlewRateLimiter {
    private final double maxDeltaPerSecond;
    private double lastOutput;
    private long lastNanos;
    private boolean initialized;

    public SlewRateLimiter(double maxDeltaPerSecond) {
        if (!(maxDeltaPerSecond > 0.0) || Double.isInfinite(maxDeltaPerSecond)) {
            throw new IllegalArgumentException("maxDeltaPerSecond must be finite and > 0");
        }
        this.maxDeltaPerSecond = maxDeltaPerSecond;
    }

    public double apply(double requested, long nowNanos) {
        if (!initialized) {
            lastOutput = requested;
            lastNanos = nowNanos;
            initialized = true;
            return requested;
        }
        long dt = nowNanos - lastNanos;
        if (dt < 0L) {
            throw new IllegalArgumentException("time must be monotonic");
        }
        lastNanos = nowNanos;
        if (dt == 0L) {
            return lastOutput;
        }
        double maxDelta = maxDeltaPerSecond * (dt / 1_000_000_000.0);
        double delta = requested - lastOutput;
        if (delta > maxDelta) {
            lastOutput += maxDelta;
        } else if (delta < -maxDelta) {
            lastOutput -= maxDelta;
        } else {
            lastOutput = requested;
        }
        return lastOutput;
    }

    public double lastOutput() {
        return lastOutput;
    }

    public void reset() {
        initialized = false;
        lastOutput = 0.0;
        lastNanos = 0L;
    }
}
