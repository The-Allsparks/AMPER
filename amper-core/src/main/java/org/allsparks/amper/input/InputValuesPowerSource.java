package org.allsparks.amper.input;

import java.util.Objects;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.measure.VoltageSample;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;

/**
 * Bus voltage from a published {@link InputValues} snapshot.
 *
 * <p>Does not call {@code VoltageSensor.getVoltage()} or any other hardware
 * getter. The sampler that implements {@link InputValues} already captured the
 * signal. AMPER filters and classifies the published sample. Reads
 * {@link InputValues#getDouble(SignalKey)} and validity metadata so the
 * observe path does not allocate a {@link org.allsparks.contracts.input.Sample}.
 *
 * <p>Hub-level battery current stays {@link MeasurementValidity#UNSUPPORTED}
 * until a verified current key exists. Do not invent total current.
 */
public final class InputValuesPowerSource implements PowerTelemetrySource {
    private final InputValues values;
    private final SignalKey<Double> voltageKey;
    private final String sourceName;

    public InputValuesPowerSource(InputValues values, SignalKey<Double> voltageKey, String sourceName) {
        this.values = Objects.requireNonNull(values, "values");
        this.voltageKey = Objects.requireNonNull(voltageKey, "voltageKey");
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
    }

    @Override
    public VoltageSample readBusVoltage(long nowNanos) {
        Validity published = values.validity(voltageKey);
        MeasurementValidity validity = mapValidity(published);
        double volts = values.getDouble(voltageKey);
        long capturedAt = values.captureTimestampNanos(voltageKey);
        if (capturedAt <= 0L) {
            capturedAt = nowNanos;
        }
        if (Double.isNaN(volts)) {
            if (validity == MeasurementValidity.VALID) {
                validity = MeasurementValidity.MISSING;
            }
            return new VoltageSample(Double.NaN, capturedAt, validity, sourceName);
        }
        return new VoltageSample(volts, capturedAt, validity, sourceName);
    }

    @Override
    public CurrentSample readBatteryCurrent(long nowNanos) {
        return CurrentSample.unsupported(nowNanos, sourceName + ":battery");
    }

    @Override
    public String sourceName() {
        return sourceName;
    }

    static MeasurementValidity mapValidity(Validity validity) {
        if (validity == null) {
            return MeasurementValidity.MISSING;
        }
        switch (validity) {
            case VALID:
                return MeasurementValidity.VALID;
            case STALE:
                return MeasurementValidity.STALE;
            case OUT_OF_RANGE:
                return MeasurementValidity.OUT_OF_RANGE;
            case UNSUPPORTED:
                return MeasurementValidity.UNSUPPORTED;
            case MISSING:
            case INVALID:
            default:
                return MeasurementValidity.MISSING;
        }
    }
}
