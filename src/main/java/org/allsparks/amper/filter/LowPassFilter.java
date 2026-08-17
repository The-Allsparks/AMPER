package org.allsparks.amper.filter;

/**
 * First-order low-pass filter for voltage (or other scalar) signals.
 * Does not invent values for invalid inputs.
 */
public final class LowPassFilter {
    private final double alpha;
    private double filtered;
    private boolean initialized;

    /**
     * @param alpha smoothing factor in (0, 1]; larger means less smoothing
     */
    public LowPassFilter(double alpha) {
        if (!(alpha > 0.0) || alpha > 1.0) {
            throw new IllegalArgumentException("alpha must be in (0, 1]");
        }
        this.alpha = alpha;
    }

    public double update(double sample) {
        if (Double.isNaN(sample)) {
            return Double.NaN;
        }
        if (!initialized) {
            filtered = sample;
            initialized = true;
            return filtered;
        }
        filtered = alpha * sample + (1.0 - alpha) * filtered;
        return filtered;
    }

    public double value() {
        return initialized ? filtered : Double.NaN;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void reset() {
        initialized = false;
        filtered = 0.0;
    }
}
