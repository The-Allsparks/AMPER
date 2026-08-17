package org.allsparks.amper.adapters.rev;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.measure.VoltageSample;

/**
 * REV Control Hub / Expansion Hub telemetry adapter.
 *
 * <p>Depends on functional suppliers so unit tests and desktop builds do not
 * require the FTC SDK on the classpath. On-robot, wire suppliers to
 * {@code VoltageSensor#getVoltage()} and hub current APIs.
 *
 * <p>This adapter never commands motors.
 */
public final class RevHubTelemetrySource implements PowerTelemetrySource {
    private final String sourceName;
    private final DoubleSupplier busVoltageVolts;
    private final Supplier<CurrentReading> batteryCurrent;

    public RevHubTelemetrySource(
            String sourceName,
            DoubleSupplier busVoltageVolts,
            Supplier<CurrentReading> batteryCurrent) {
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
        this.busVoltageVolts = Objects.requireNonNull(busVoltageVolts, "busVoltageVolts");
        this.batteryCurrent = batteryCurrent;
    }

    public static RevHubTelemetrySource voltageOnly(String sourceName, DoubleSupplier busVoltageVolts) {
        return new RevHubTelemetrySource(sourceName, busVoltageVolts, null);
    }

    @Override
    public VoltageSample readBusVoltage(long nowNanos) {
        try {
            double volts = busVoltageVolts.getAsDouble();
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
        if (batteryCurrent == null) {
            return CurrentSample.unsupported(nowNanos, sourceName + ":battery");
        }
        try {
            CurrentReading reading = batteryCurrent.get();
            if (reading == null || !reading.supported) {
                return CurrentSample.unsupported(nowNanos, sourceName + ":battery");
            }
            if (Double.isNaN(reading.amps)) {
                return CurrentSample.missing(nowNanos, sourceName + ":battery");
            }
            return new CurrentSample(
                    reading.amps, nowNanos, MeasurementValidity.VALID, sourceName + ":battery");
        } catch (RuntimeException ex) {
            return CurrentSample.missing(nowNanos, sourceName + ":battery");
        }
    }

    @Override
    public String sourceName() {
        return sourceName;
    }

    /** Simple current payload for supplier wiring. */
    public static final class CurrentReading {
        public final boolean supported;
        public final double amps;

        public CurrentReading(boolean supported, double amps) {
            this.supported = supported;
            this.amps = amps;
        }

        public static CurrentReading ofAmps(double amps) {
            return new CurrentReading(true, amps);
        }

        public static CurrentReading unsupported() {
            return new CurrentReading(false, Double.NaN);
        }
    }
}
