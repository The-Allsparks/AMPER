package org.allsparks.amper.sim;

import org.allsparks.amper.clock.AmperClock;

/** Deterministic clock for replay and unit tests. */
public final class SimulatedClock implements AmperClock {
    private long nanos;

    public SimulatedClock() {
        this(0L);
    }

    public SimulatedClock(long startNanos) {
        this.nanos = startNanos;
    }

    @Override
    public long nanoTime() {
        return nanos;
    }

    public void set(long value) {
        this.nanos = value;
    }

    public void advance(long deltaNanos) {
        if (deltaNanos < 0L) {
            throw new IllegalArgumentException("delta must be nonnegative");
        }
        nanos += deltaNanos;
    }
}
