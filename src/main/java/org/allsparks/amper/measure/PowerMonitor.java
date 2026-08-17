package org.allsparks.amper.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.filter.LowPassFilter;
import org.allsparks.amper.filter.MinTracker;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Passive electrical monitor. Reads and filters observations; never commands
 * hardware.
 */
public final class PowerMonitor {
    private final AmperClock clock;
    private final PowerTelemetrySource telemetrySource;
    private final List<MotorElectricalTelemetry> motors;
    private final LowPassFilter voltageFilter;
    private final MinTracker voltageMinimum;
    private final long staleAfterNanos;
    private final double minValidVolts;
    private final double maxValidVolts;

    private ElectricalObservation lastObservation;

    public PowerMonitor(
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors,
            double voltageFilterAlpha,
            long staleAfterNanos,
            double minValidVolts,
            double maxValidVolts) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.telemetrySource = Objects.requireNonNull(telemetrySource, "telemetrySource");
        this.motors = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(motors, "motors")));
        this.voltageFilter = new LowPassFilter(voltageFilterAlpha);
        this.voltageMinimum = new MinTracker();
        this.staleAfterNanos = staleAfterNanos;
        this.minValidVolts = minValidVolts;
        this.maxValidVolts = maxValidVolts;
    }

    public static PowerMonitor create(
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors,
            PowerPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return new PowerMonitor(
                clock,
                telemetrySource,
                motors,
                policy.voltageFilterAlpha(),
                policy.staleAfterNanos(),
                policy.minValidVolts(),
                policy.maxValidVolts());
    }

    public ElectricalObservation update() {
        long loopStart = clock.nanoTime();

        VoltageSample raw = classifyVoltage(telemetrySource.readBusVoltage(loopStart), loopStart);
        VoltageSample filtered;
        if (raw.isUsable()) {
            double filteredVolts = voltageFilter.update(raw.volts());
            filtered = new VoltageSample(
                    filteredVolts, loopStart, MeasurementValidity.VALID, raw.sourceId());
            voltageMinimum.offer(raw.volts());
        } else {
            filtered = new VoltageSample(
                    voltageFilter.value(), loopStart, raw.validity(), raw.sourceId());
        }

        CurrentSample batteryCurrent = telemetrySource.readBatteryCurrent(loopStart);
        List<MotorSnapshot> snapshots = new ArrayList<>(motors.size());
        for (MotorElectricalTelemetry motor : motors) {
            snapshots.add(readMotor(motor, loopStart));
        }

        boolean sensingValid = raw.isUsable();
        long loopDuration = Math.max(0L, clock.nanoTime() - loopStart);

        lastObservation = new ElectricalObservation(
                loopStart,
                loopDuration,
                raw,
                filtered,
                voltageMinimum.minimumOrNaN(),
                batteryCurrent,
                snapshots,
                sensingValid);
        return lastObservation;
    }

    public ElectricalObservation lastObservation() {
        return lastObservation;
    }

    public void resetMatchStatistics() {
        voltageMinimum.reset();
        voltageFilter.reset();
        lastObservation = null;
    }

    private VoltageSample classifyVoltage(VoltageSample sample, long nowNanos) {
        if (sample == null) {
            return VoltageSample.missing(nowNanos, telemetrySource.sourceName());
        }
        if (sample.validity() != MeasurementValidity.VALID) {
            return sample;
        }
        if (nowNanos - sample.capturedAtNanos() > staleAfterNanos) {
            return new VoltageSample(
                    sample.volts(), sample.capturedAtNanos(), MeasurementValidity.STALE, sample.sourceId());
        }
        if (Double.isNaN(sample.volts())
                || sample.volts() < minValidVolts
                || sample.volts() > maxValidVolts) {
            return new VoltageSample(
                    sample.volts(), sample.capturedAtNanos(), MeasurementValidity.OUT_OF_RANGE, sample.sourceId());
        }
        return sample;
    }

    private static MotorSnapshot readMotor(MotorElectricalTelemetry motor, long nowNanos) {
        CurrentSample current;
        try {
            current = motor.readCurrent(nowNanos);
        } catch (RuntimeException ex) {
            current = CurrentSample.missing(nowNanos, motor.motorId());
        }
        return new MotorSnapshot(
                motor.motorId(),
                current,
                readDoubleQuietly(motor::commandedEffort),
                readDoubleQuietly(motor::velocityTicksPerSecond),
                readDoubleQuietly(motor::positionTicks));
    }

    private static double readDoubleQuietly(DoubleRead read) {
        try {
            return read.get();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    private interface DoubleRead {
        double get();
    }
}
