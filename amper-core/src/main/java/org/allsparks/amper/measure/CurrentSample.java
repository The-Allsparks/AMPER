package org.allsparks.amper.measure;

/** Immutable current observation with capture time and validity. */
public final class CurrentSample {
    private final double amps;
    private final long capturedAtNanos;
    private final MeasurementValidity validity;
    private final String channelId;

    public CurrentSample(double amps, long capturedAtNanos, MeasurementValidity validity, String channelId) {
        if (validity == null) {
            throw new IllegalArgumentException("validity is required");
        }
        this.amps = amps;
        this.capturedAtNanos = capturedAtNanos;
        this.validity = validity;
        this.channelId = channelId == null ? "" : channelId;
    }

    public static CurrentSample missing(long capturedAtNanos, String channelId) {
        return new CurrentSample(Double.NaN, capturedAtNanos, MeasurementValidity.MISSING, channelId);
    }

    public static CurrentSample unsupported(long capturedAtNanos, String channelId) {
        return new CurrentSample(Double.NaN, capturedAtNanos, MeasurementValidity.UNSUPPORTED, channelId);
    }

    public static CurrentSample skipped(long capturedAtNanos, String channelId) {
        return new CurrentSample(Double.NaN, capturedAtNanos, MeasurementValidity.SKIPPED, channelId);
    }

    public static CurrentSample skippedCarry(CurrentSample previous, long nowNanos, long staleAfterNanos) {
        if (previous == null) {
            return skipped(nowNanos, "");
        }
        MeasurementValidity validity = MeasurementValidity.SKIPPED;
        if (nowNanos - previous.capturedAtNanos() > staleAfterNanos) {
            validity = MeasurementValidity.STALE;
        }
        return new CurrentSample(previous.amps(), previous.capturedAtNanos(), validity, previous.channelId());
    }

    public double amps() {
        return amps;
    }

    public long capturedAtNanos() {
        return capturedAtNanos;
    }

    public long ageNanos(long nowNanos) {
        return Math.max(0L, nowNanos - capturedAtNanos);
    }

    public MeasurementValidity validity() {
        return validity;
    }

    public String channelId() {
        return channelId;
    }

    public boolean isUsable() {
        return validity == MeasurementValidity.VALID && !Double.isNaN(amps);
    }
}
