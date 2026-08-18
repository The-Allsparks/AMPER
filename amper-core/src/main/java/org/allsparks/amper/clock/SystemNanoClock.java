package org.allsparks.amper.clock;

/** Production clock backed by {@link System#nanoTime()}. */
public final class SystemNanoClock implements AmperClock {
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
