package org.allsparks.amper.measure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.filter.LowPassFilter;
import org.allsparks.amper.filter.MinTracker;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;

/**
 * Passive electrical monitor. Reads and filters observations; never commands
 * hardware.
 */
public final class PowerMonitor {
    private final AmperClock clock;
    private final List<PowerTelemetrySource> telemetrySources;
    private final int policySourceIndex;
    private final List<MotorElectricalTelemetry> motors;
    private final LowPassFilter voltageFilter;
    private final MinTracker voltageMinimum;
    private final long staleAfterNanos;
    private final double minValidVolts;
    private final double maxValidVolts;
    private final SamplingPolicy sampling;
    private final double mechanismStartEffort;

    private final CurrentSample[] lastCurrents;
    private final VoltageSample[] lastVoltages;
    private final long[] lastVoltageReadNanos;
    private final long[] lastCurrentReadNanos;
    private final long[] lastVelocityReadNanos;
    private final long[] lastCommandReadNanos;
    private final double[] lastVelocity;
    private final double[] lastCommand;
    private final double[] lastPosition;
    private final boolean[] readCurrentScratch;
    private final List<VoltageSample> voltageScratch;
    private final List<MotorSnapshot> motorScratch;

    private int roundRobinIndex;
    private long failedTotal;
    private long staleTotal;
    private long unsupportedTotal;
    private long skippedTotal;
    private ElectricalObservation lastObservation;

    public PowerMonitor(
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors,
            double voltageFilterAlpha,
            long staleAfterNanos,
            double minValidVolts,
            double maxValidVolts) {
        this(
                clock,
                Collections.singletonList(telemetrySource),
                0,
                motors,
                voltageFilterAlpha,
                staleAfterNanos,
                minValidVolts,
                maxValidVolts,
                SamplingPolicy.everyLoop(),
                0.10);
    }

    public PowerMonitor(
            AmperClock clock,
            List<PowerTelemetrySource> telemetrySources,
            int policySourceIndex,
            List<MotorElectricalTelemetry> motors,
            double voltageFilterAlpha,
            long staleAfterNanos,
            double minValidVolts,
            double maxValidVolts,
            SamplingPolicy sampling,
            double mechanismStartEffort) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.telemetrySources = Collections.unmodifiableList(new ArrayList<PowerTelemetrySource>(
                Objects.requireNonNull(telemetrySources, "telemetrySources")));
        if (this.telemetrySources.isEmpty()) {
            throw new IllegalArgumentException("at least one voltage source is required");
        }
        if (policySourceIndex < 0 || policySourceIndex >= this.telemetrySources.size()) {
            throw new IllegalArgumentException("policySourceIndex out of range");
        }
        this.policySourceIndex = policySourceIndex;
        this.motors = Collections.unmodifiableList(new ArrayList<MotorElectricalTelemetry>(
                Objects.requireNonNull(motors, "motors")));
        this.voltageFilter = new LowPassFilter(voltageFilterAlpha);
        this.voltageMinimum = new MinTracker();
        this.staleAfterNanos = staleAfterNanos;
        this.minValidVolts = minValidVolts;
        this.maxValidVolts = maxValidVolts;
        this.sampling = sampling == null ? SamplingPolicy.everyLoop() : sampling;
        this.mechanismStartEffort = mechanismStartEffort;
        int sourceCount = this.telemetrySources.size();
        int motorCount = this.motors.size();
        this.lastVoltages = new VoltageSample[sourceCount];
        this.lastVoltageReadNanos = unreadTimes(sourceCount);
        this.lastCurrents = new CurrentSample[motorCount];
        this.lastCurrentReadNanos = unreadTimes(motorCount);
        this.lastVelocityReadNanos = unreadTimes(motorCount);
        this.lastCommandReadNanos = unreadTimes(motorCount);
        this.lastVelocity = nanArray(motorCount);
        this.lastCommand = nanArray(motorCount);
        this.lastPosition = nanArray(motorCount);
        this.readCurrentScratch = new boolean[motorCount];
        this.voltageScratch = new ArrayList<VoltageSample>(sourceCount);
        this.motorScratch = new ArrayList<MotorSnapshot>(motorCount);
    }

    public static PowerMonitor create(
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors,
            PowerPolicy policy) {
        return create(clock, Collections.singletonList(telemetrySource), 0, motors, policy);
    }

    public static PowerMonitor create(
            AmperClock clock,
            List<PowerTelemetrySource> telemetrySources,
            int policySourceIndex,
            List<MotorElectricalTelemetry> motors,
            PowerPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return new PowerMonitor(
                clock,
                telemetrySources,
                policySourceIndex,
                motors,
                policy.voltageFilterAlpha(),
                policy.staleAfterNanos(),
                policy.minValidVolts(),
                policy.maxValidVolts(),
                policy.sampling(),
                policy.mechanismStartEffort());
    }

    public ElectricalObservation update() {
        long loopStart = clock.nanoTime();
        long failed = 0;
        long stale = 0;
        long unsupported = 0;
        long skipped = 0;

        voltageScratch.clear();
        for (int i = 0; i < telemetrySources.size(); i++) {
            PowerTelemetrySource source = telemetrySources.get(i);
            boolean due = due(lastVoltageReadNanos[i], loopStart, sampling.voltagePeriodNanos());
            VoltageSample sample;
            if (due) {
                sample = classifyVoltage(source.readBusVoltage(loopStart), loopStart, source.sourceName());
                lastVoltageReadNanos[i] = loopStart;
                lastVoltages[i] = sample;
            } else {
                sample = VoltageSample.skippedCarry(
                        lastVoltages[i], loopStart, source.sourceName(), staleAfterNanos);
                skipped++;
            }
            voltageScratch.add(sample);
            failed += countFailed(sample.validity());
            stale += sample.validity() == MeasurementValidity.STALE ? 1 : 0;
            unsupported += sample.validity() == MeasurementValidity.UNSUPPORTED ? 1 : 0;
        }

        VoltageSample raw = voltageScratch.get(policySourceIndex);
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

        PowerTelemetrySource policySource = telemetrySources.get(policySourceIndex);
        CurrentSample batteryCurrent = policySource.readBatteryCurrent(loopStart);
        failed += countFailed(batteryCurrent.validity());
        unsupported += batteryCurrent.validity() == MeasurementValidity.UNSUPPORTED ? 1 : 0;

        int currentBudget = sampling.maxCurrentReadsPerLoop();
        int currentReads = 0;
        motorScratch.clear();
        if (!motors.isEmpty() && currentBudget > 0) {
            // Round-robin starting point; wrap so every motor is visited.
            int start = roundRobinIndex % motors.size();
            for (int i = 0; i < readCurrentScratch.length; i++) {
                readCurrentScratch[i] = false;
            }
            for (int n = 0; n < motors.size() && currentReads < currentBudget; n++) {
                int index = (start + n) % motors.size();
                boolean currentDue = due(
                        lastCurrentReadNanos[index], loopStart, sampling.currentPeriodNanos());
                if (currentDue) {
                    readCurrentScratch[index] = true;
                    currentReads++;
                    lastCurrentReadNanos[index] = loopStart;
                }
            }
            roundRobinIndex = (start + Math.max(1, currentReads)) % motors.size();
            for (int i = 0; i < motors.size(); i++) {
                MotorSnapshot snap = readMotor(i, loopStart, readCurrentScratch[i]);
                motorScratch.add(snap);
                MeasurementValidity cv = snap.current().validity();
                failed += countFailed(cv);
                stale += cv == MeasurementValidity.STALE ? 1 : 0;
                unsupported += cv == MeasurementValidity.UNSUPPORTED ? 1 : 0;
                skipped += cv == MeasurementValidity.SKIPPED ? 1 : 0;
            }
        } else {
            for (int i = 0; i < motors.size(); i++) {
                MotorSnapshot snap = readMotor(i, loopStart, false);
                motorScratch.add(snap);
                skipped++;
            }
        }

        boolean sensingValid = raw.isUsable();
        long loopDuration = Math.max(0L, clock.nanoTime() - loopStart);
        failedTotal += failed;
        staleTotal += stale;
        unsupportedTotal += unsupported;
        skippedTotal += skipped;

        lastObservation = new ElectricalObservation(
                loopStart,
                loopDuration,
                raw,
                filtered,
                voltageMinimum.minimumOrNaN(),
                batteryCurrent,
                motorScratch,
                sensingValid,
                voltageScratch,
                new SamplingStats(
                        failed,
                        stale,
                        unsupported,
                        skipped,
                        currentReads,
                        failedTotal,
                        staleTotal,
                        unsupportedTotal,
                        skippedTotal,
                        0),
                false);
        return lastObservation;
    }

    public ElectricalObservation lastObservation() {
        return lastObservation;
    }

    public void resetMatchStatistics() {
        voltageMinimum.reset();
        voltageFilter.reset();
        lastObservation = null;
        Arrays.fill(lastCurrents, null);
        Arrays.fill(lastVoltages, null);
        Arrays.fill(lastVoltageReadNanos, Long.MIN_VALUE);
        Arrays.fill(lastCurrentReadNanos, Long.MIN_VALUE);
        Arrays.fill(lastVelocityReadNanos, Long.MIN_VALUE);
        Arrays.fill(lastCommandReadNanos, Long.MIN_VALUE);
        Arrays.fill(lastVelocity, Double.NaN);
        Arrays.fill(lastCommand, Double.NaN);
        Arrays.fill(lastPosition, Double.NaN);
        voltageScratch.clear();
        motorScratch.clear();
        roundRobinIndex = 0;
        failedTotal = 0;
        staleTotal = 0;
        unsupportedTotal = 0;
        skippedTotal = 0;
    }

    public List<PowerTelemetrySource> telemetrySources() {
        return telemetrySources;
    }

    public int policySourceIndex() {
        return policySourceIndex;
    }

    private VoltageSample classifyVoltage(VoltageSample sample, long nowNanos, String sourceName) {
        if (sample == null) {
            return VoltageSample.missing(nowNanos, sourceName);
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

    private MotorSnapshot readMotor(int index, long nowNanos, boolean readCurrent) {
        MotorElectricalTelemetry motor = motors.get(index);
        CurrentSample current;
        if (readCurrent) {
            try {
                current = motor.readCurrent(nowNanos);
            } catch (RuntimeException ex) {
                current = CurrentSample.missing(nowNanos, motor.motorId());
            }
            lastCurrents[index] = current;
        } else {
            current = CurrentSample.skippedCarry(lastCurrents[index], nowNanos, staleAfterNanos);
            if (current.channelId() == null || current.channelId().isEmpty()) {
                current = new CurrentSample(
                        current.amps(), current.capturedAtNanos(), current.validity(), motor.motorId());
            }
        }

        boolean commandDue = due(lastCommandReadNanos[index], nowNanos, sampling.commandPeriodNanos());
        double command;
        if (commandDue) {
            command = readCommandedEffort(motor);
            lastCommand[index] = command;
            lastCommandReadNanos[index] = nowNanos;
        } else {
            command = lastCommand[index];
        }

        boolean velocityDue = due(lastVelocityReadNanos[index], nowNanos, sampling.velocityPeriodNanos());
        double velocity;
        double position;
        if (velocityDue) {
            velocity = readVelocity(motor);
            position = readPosition(motor);
            lastVelocity[index] = velocity;
            lastPosition[index] = position;
            lastVelocityReadNanos[index] = nowNanos;
        } else {
            velocity = lastVelocity[index];
            position = lastPosition[index];
        }

        boolean active = !Double.isNaN(command) && Math.abs(command) >= mechanismStartEffort;
        return new MotorSnapshot(
                motor.motorId(), current, command, velocity, position, readCurrent, active);
    }

    private static boolean due(long lastNanos, long nowNanos, long periodNanos) {
        if (periodNanos <= 0L || lastNanos == Long.MIN_VALUE) {
            return true;
        }
        return nowNanos - lastNanos >= periodNanos;
    }

    private static long[] unreadTimes(int length) {
        long[] times = new long[length];
        Arrays.fill(times, Long.MIN_VALUE);
        return times;
    }

    private static double[] nanArray(int length) {
        double[] values = new double[length];
        Arrays.fill(values, Double.NaN);
        return values;
    }

    private static long countFailed(MeasurementValidity validity) {
        if (validity == MeasurementValidity.MISSING || validity == MeasurementValidity.OUT_OF_RANGE) {
            return 1L;
        }
        return 0L;
    }

    private static double readCommandedEffort(MotorElectricalTelemetry motor) {
        try {
            return motor.commandedEffort();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    private static double readVelocity(MotorElectricalTelemetry motor) {
        try {
            return motor.velocityTicksPerSecond();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    private static double readPosition(MotorElectricalTelemetry motor) {
        try {
            return motor.positionTicks();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }
}
