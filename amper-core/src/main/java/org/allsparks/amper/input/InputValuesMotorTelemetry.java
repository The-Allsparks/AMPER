package org.allsparks.amper.input;

import java.util.Objects;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;

/**
 * Per-motor current from a published {@link InputValues} snapshot.
 *
 * <p>Never calls Hub {@code getCurrent}. Fresh VALID samples are recorded;
 * otherwise the last sample is carried as skipped/stale.
 */
public final class InputValuesMotorTelemetry implements MotorElectricalTelemetry {
    private final String motorId;
    private final InputValues values;
    private final SignalKey<Double> currentKey;
    private final long staleAfterNanos;
    private CurrentSample last;

    public InputValuesMotorTelemetry(
            String motorId, InputValues values, SignalKey<Double> currentKey, long staleAfterNanos) {
        this.motorId = Objects.requireNonNull(motorId, "motorId");
        this.values = Objects.requireNonNull(values, "values");
        this.currentKey = Objects.requireNonNull(currentKey, "currentKey");
        if (staleAfterNanos <= 0L) {
            throw new IllegalArgumentException("staleAfterNanos must be > 0");
        }
        this.staleAfterNanos = staleAfterNanos;
    }

    @Override
    public String motorId() {
        return motorId;
    }

    @Override
    public boolean currentIsCachePeek() {
        return true;
    }

    @Override
    public CurrentSample readCurrent(long nowNanos) {
        double amps = values.tryGetDouble(currentKey);
        if (Double.isNaN(amps) && !values.contains(currentKey)) {
            return CurrentSample.skippedCarry(last, nowNanos, staleAfterNanos);
        }
        if (values.isFresh(currentKey) && values.validity(currentKey) == Validity.VALID) {
            if (Double.isNaN(amps)) {
                last = CurrentSample.missing(nowNanos, motorId);
                return last;
            }
            last = new CurrentSample(
                    amps, values.captureTimestampNanos(currentKey), MeasurementValidity.VALID, motorId);
            return last;
        }
        return CurrentSample.skippedCarry(last, nowNanos, staleAfterNanos);
    }

    @Override
    public double commandedEffort() {
        return Double.NaN;
    }

    @Override
    public double velocityTicksPerSecond() {
        return Double.NaN;
    }

    @Override
    public double positionTicks() {
        return Double.NaN;
    }
}
