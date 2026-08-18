package org.allsparks.amper.measure;

/**
 * Counts of sampling outcomes for one update and for the match so far.
 * Does not invent electrical values.
 */
public final class SamplingStats {
    private final long failedThisLoop;
    private final long staleThisLoop;
    private final long unsupportedThisLoop;
    private final long skippedThisLoop;
    private final int currentReadsThisLoop;
    private final long failedTotal;
    private final long staleTotal;
    private final long unsupportedTotal;
    private final long skippedTotal;
    private final int duplicateObserves;

    public SamplingStats(
            long failedThisLoop,
            long staleThisLoop,
            long unsupportedThisLoop,
            long skippedThisLoop,
            int currentReadsThisLoop,
            long failedTotal,
            long staleTotal,
            long unsupportedTotal,
            long skippedTotal,
            int duplicateObserves) {
        this.failedThisLoop = failedThisLoop;
        this.staleThisLoop = staleThisLoop;
        this.unsupportedThisLoop = unsupportedThisLoop;
        this.skippedThisLoop = skippedThisLoop;
        this.currentReadsThisLoop = currentReadsThisLoop;
        this.failedTotal = failedTotal;
        this.staleTotal = staleTotal;
        this.unsupportedTotal = unsupportedTotal;
        this.skippedTotal = skippedTotal;
        this.duplicateObserves = duplicateObserves;
    }

    public static SamplingStats empty() {
        return new SamplingStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public long failedThisLoop() {
        return failedThisLoop;
    }

    public long staleThisLoop() {
        return staleThisLoop;
    }

    public long unsupportedThisLoop() {
        return unsupportedThisLoop;
    }

    public long skippedThisLoop() {
        return skippedThisLoop;
    }

    public int currentReadsThisLoop() {
        return currentReadsThisLoop;
    }

    public long failedTotal() {
        return failedTotal;
    }

    public long staleTotal() {
        return staleTotal;
    }

    public long unsupportedTotal() {
        return unsupportedTotal;
    }

    public long skippedTotal() {
        return skippedTotal;
    }

    public int duplicateObserves() {
        return duplicateObserves;
    }
}
