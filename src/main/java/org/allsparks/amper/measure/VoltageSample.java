package org.allsparks.amper.measure;

/** Immutable voltage observation with capture time and validity. */
public final class VoltageSample {
    private final double volts;
    private final long capturedAtNanos;
    private final MeasurementValidity validity;
    private final String sourceId;

    public VoltageSample(double volts, long capturedAtNanos, MeasurementValidity validity, String sourceId) {
        if (validity == null) {
            throw new IllegalArgumentException("validity is required");
        }
        this.volts = volts;
        this.capturedAtNanos = capturedAtNanos;
        this.validity = validity;
        this.sourceId = sourceId == null ? "" : sourceId;
    }

    public static VoltageSample missing(long capturedAtNanos, String sourceId) {
        return new VoltageSample(Double.NaN, capturedAtNanos, MeasurementValidity.MISSING, sourceId);
    }

    public static VoltageSample unsupported(long capturedAtNanos, String sourceId) {
        return new VoltageSample(Double.NaN, capturedAtNanos, MeasurementValidity.UNSUPPORTED, sourceId);
    }

    public double volts() {
        return volts;
    }

    public long capturedAtNanos() {
        return capturedAtNanos;
    }

    public MeasurementValidity validity() {
        return validity;
    }

    public String sourceId() {
        return sourceId;
    }

    public boolean isUsable() {
        return validity == MeasurementValidity.VALID && !Double.isNaN(volts);
    }
}
