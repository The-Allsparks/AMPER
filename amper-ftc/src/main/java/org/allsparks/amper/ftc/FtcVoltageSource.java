package org.allsparks.amper.ftc;

import com.qualcomm.robotcore.hardware.VoltageSensor;
import java.util.Objects;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.measure.VoltageSample;

/**
 * FTC {@link VoltageSensor} adapter. Catches expected hardware-read failures.
 * Does not invent battery current: hub-level battery current is reported
 * {@link MeasurementValidity#UNSUPPORTED} unless a caller supplies a verified API.
 */
public final class FtcVoltageSource implements PowerTelemetrySource {
    private final String sourceName;
    private final VoltageSensor sensor;

    public FtcVoltageSource(String sourceName, VoltageSensor sensor) {
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
        this.sensor = Objects.requireNonNull(sensor, "sensor");
    }

    @Override
    public VoltageSample readBusVoltage(long nowNanos) {
        try {
            double volts = sensor.getVoltage();
            if (Double.isNaN(volts)) {
                return VoltageSample.missing(nowNanos, sourceName);
            }
            return new VoltageSample(volts, nowNanos, MeasurementValidity.VALID, sourceName);
        } catch (RuntimeException ex) {
            return VoltageSample.missing(nowNanos, sourceName);
        }
    }

    @Override
    public CurrentSample readBatteryCurrent(long nowNanos) {
        return CurrentSample.unsupported(nowNanos, sourceName + ":battery");
    }

    @Override
    public String sourceName() {
        return sourceName;
    }
}
