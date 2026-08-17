package org.allsparks.amper.filter;

/** Tracks the minimum valid sample over a window that can be reset each match. */
public final class MinTracker {
    private double minimum = Double.POSITIVE_INFINITY;
    private boolean hasValue;

    public void offer(double sample) {
        if (Double.isNaN(sample)) {
            return;
        }
        if (!hasValue || sample < minimum) {
            minimum = sample;
            hasValue = true;
        }
    }

    public double minimumOrNaN() {
        return hasValue ? minimum : Double.NaN;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public void reset() {
        minimum = Double.POSITIVE_INFINITY;
        hasValue = false;
    }
}
