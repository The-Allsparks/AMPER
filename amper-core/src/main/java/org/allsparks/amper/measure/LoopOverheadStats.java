package org.allsparks.amper.measure;

import java.util.Arrays;

/**
 * Running min / mean / max plus a small histogram and a rolling window for
 * percentile estimates of AMPER update duration.
 */
public final class LoopOverheadStats {
    private static final int WINDOW = 256;
    private static final long[] BUCKET_MAX_NS = {
        50_000L,
        100_000L,
        200_000L,
        500_000L,
        1_000_000L,
        2_000_000L,
        5_000_000L,
        Long.MAX_VALUE
    };

    private long count;
    private long totalNanos;
    private long maxNanos;
    private long minNanos = Long.MAX_VALUE;
    private final long[] buckets = new long[BUCKET_MAX_NS.length];
    private final long[] window = new long[WINDOW];
    private int windowSize;
    private int windowIndex;

    public void offer(long durationNanos) {
        if (durationNanos < 0L) {
            return;
        }
        count++;
        totalNanos += durationNanos;
        if (durationNanos > maxNanos) {
            maxNanos = durationNanos;
        }
        if (durationNanos < minNanos) {
            minNanos = durationNanos;
        }
        for (int i = 0; i < BUCKET_MAX_NS.length; i++) {
            if (durationNanos <= BUCKET_MAX_NS[i]) {
                buckets[i]++;
                break;
            }
        }
        window[windowIndex] = durationNanos;
        windowIndex = (windowIndex + 1) % WINDOW;
        if (windowSize < WINDOW) {
            windowSize++;
        }
    }

    public long count() {
        return count;
    }

    public long maxNanos() {
        return count == 0L ? 0L : maxNanos;
    }

    public long minNanos() {
        return count == 0L ? 0L : minNanos;
    }

    public double meanNanos() {
        if (count == 0L) {
            return Double.NaN;
        }
        return (double) totalNanos / (double) count;
    }

    /** Approximate percentile over the most recent samples (up to 256). */
    public long percentileNanos(double percentile) {
        if (windowSize == 0) {
            return 0L;
        }
        long[] copy = Arrays.copyOf(window, windowSize);
        Arrays.sort(copy);
        double clamped = percentile;
        if (clamped < 0.0) {
            clamped = 0.0;
        }
        if (clamped > 1.0) {
            clamped = 1.0;
        }
        int index = (int) Math.round(clamped * (copy.length - 1));
        return copy[index];
    }

    public long[] histogramBuckets() {
        return Arrays.copyOf(buckets, buckets.length);
    }

    public void reset() {
        count = 0L;
        totalNanos = 0L;
        maxNanos = 0L;
        minNanos = Long.MAX_VALUE;
        Arrays.fill(buckets, 0L);
        windowSize = 0;
        windowIndex = 0;
        Arrays.fill(window, 0L);
    }
}
