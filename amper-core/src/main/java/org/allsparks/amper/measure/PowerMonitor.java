package org.allsparks.amper.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<String, CurrentSample> lastCurrents = new LinkedHashMap<String, CurrentSample>();
    private final Map<String, VoltageSample> lastVoltages = new LinkedHashMap<String, VoltageSample>();
    private final Map<String, Long> lastVoltageReadNanos = new LinkedHashMap<String, Long>();
    private final Map<String, Long> lastCurrentReadNanos = new LinkedHashMap<String, Long>();
    private final Map<String, Long> lastVelocityReadNanos = new LinkedHashMap<String, Long>();
    private final Map<String, Long> lastCommandReadNanos = new LinkedHashMap<String, Long>();
    private final Map<String, Double> lastVelocity = new LinkedHashMap<String, Double>();
    private final Map<String, Double> lastCommand = new LinkedHashMap<String, Double>();
    private final Map<String, Double> lastPosition = new LinkedHashMap<String, Double>();

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

        List<VoltageSample> voltages = new ArrayList<VoltageSample>(telemetrySources.size());
        for (int i = 0; i < telemetrySources.size(); i++) {
            PowerTelemetrySource source = telemetrySources.get(i);
            boolean due = due(lastVoltageReadNanos.get(source.sourceName()), loopStart, sampling.voltagePeriodNanos());
            VoltageSample sample;
            if (due) {
                sample = classifyVoltage(source.readBusVoltage(loopStart), loopStart, source.sourceName());
                lastVoltageReadNanos.put(source.sourceName(), loopStart);
                lastVoltages.put(source.sourceName(), sample);
            } else {
                sample = VoltageSample.skippedCarry(
                        lastVoltages.get(source.sourceName()), loopStart, source.sourceName(), staleAfterNanos);
                skipped++;
            }
            voltages.add(sample);
            failed += countFailed(sample.validity());
            stale += sample.validity() == MeasurementValidity.STALE ? 1 : 0;
            unsupported += sample.validity() == MeasurementValidity.UNSUPPORTED ? 1 : 0;
        }

        VoltageSample raw = voltages.get(policySourceIndex);
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
        List<MotorSnapshot> snapshots = new ArrayList<MotorSnapshot>(motors.size());
        if (!motors.isEmpty() && currentBudget > 0) {
            // Round-robin starting point; wrap so every motor is visited.
            int start = roundRobinIndex % motors.size();
            boolean[] readCurrent = new boolean[motors.size()];
            for (int n = 0; n < motors.size() && currentReads < currentBudget; n++) {
                int index = (start + n) % motors.size();
                MotorElectricalTelemetry motor = motors.get(index);
                boolean currentDue = due(
                        lastCurrentReadNanos.get(motor.motorId()), loopStart, sampling.currentPeriodNanos());
                if (currentDue) {
                    readCurrent[index] = true;
                    currentReads++;
                    lastCurrentReadNanos.put(motor.motorId(), loopStart);
                }
            }
            roundRobinIndex = (start + Math.max(1, currentReads)) % motors.size();
            for (int i = 0; i < motors.size(); i++) {
                MotorSnapshot snap = readMotor(motors.get(i), loopStart, readCurrent[i]);
                snapshots.add(snap);
                MeasurementValidity cv = snap.current().validity();
                failed += countFailed(cv);
                stale += cv == MeasurementValidity.STALE ? 1 : 0;
                unsupported += cv == MeasurementValidity.UNSUPPORTED ? 1 : 0;
                skipped += cv == MeasurementValidity.SKIPPED ? 1 : 0;
            }
        } else {
            for (MotorElectricalTelemetry motor : motors) {
                MotorSnapshot snap = readMotor(motor, loopStart, false);
                snapshots.add(snap);
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
                snapshots,
                sensingValid,
                voltages,
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
        lastCurrents.clear();
        lastVoltages.clear();
        lastVoltageReadNanos.clear();
        lastCurrentReadNanos.clear();
        lastVelocityReadNanos.clear();
        lastCommandReadNanos.clear();
        lastVelocity.clear();
        lastCommand.clear();
        lastPosition.clear();
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

    private MotorSnapshot readMotor(MotorElectricalTelemetry motor, long nowNanos, boolean readCurrent) {
        CurrentSample current;
        if (readCurrent) {
            try {
                current = motor.readCurrent(nowNanos);
            } catch (RuntimeException ex) {
                current = CurrentSample.missing(nowNanos, motor.motorId());
            }
            lastCurrents.put(motor.motorId(), current);
        } else {
            current = CurrentSample.skippedCarry(
                    lastCurrents.get(motor.motorId()), nowNanos, staleAfterNanos);
            if (current.channelId() == null || current.channelId().isEmpty()) {
                current = new CurrentSample(
                        current.amps(), current.capturedAtNanos(), current.validity(), motor.motorId());
            }
        }

        boolean commandDue = due(lastCommandReadNanos.get(motor.motorId()), nowNanos, sampling.commandPeriodNanos());
        double command;
        if (commandDue) {
            command = readDoubleQuietly(new DoubleRead() {
                @Override
                public double get() {
                    return motor.commandedEffort();
                }
            });
            lastCommand.put(motor.motorId(), command);
            lastCommandReadNanos.put(motor.motorId(), nowNanos);
        } else {
            Double last = lastCommand.get(motor.motorId());
            command = last == null ? Double.NaN : last.doubleValue();
        }

        boolean velocityDue = due(
                lastVelocityReadNanos.get(motor.motorId()), nowNanos, sampling.velocityPeriodNanos());
        double velocity;
        double position;
        if (velocityDue) {
            velocity = readDoubleQuietly(new DoubleRead() {
                @Override
                public double get() {
                    return motor.velocityTicksPerSecond();
                }
            });
            position = readDoubleQuietly(new DoubleRead() {
                @Override
                public double get() {
                    return motor.positionTicks();
                }
            });
            lastVelocity.put(motor.motorId(), velocity);
            lastPosition.put(motor.motorId(), position);
            lastVelocityReadNanos.put(motor.motorId(), nowNanos);
        } else {
            Double lastV = lastVelocity.get(motor.motorId());
            Double lastP = lastPosition.get(motor.motorId());
            velocity = lastV == null ? Double.NaN : lastV.doubleValue();
            position = lastP == null ? Double.NaN : lastP.doubleValue();
        }

        boolean active = !Double.isNaN(command) && Math.abs(command) >= mechanismStartEffort;
        return new MotorSnapshot(
                motor.motorId(), current, command, velocity, position, readCurrent, active);
    }

    private static boolean due(Long lastNanos, long nowNanos, long periodNanos) {
        if (periodNanos <= 0L || lastNanos == null) {
            return true;
        }
        return nowNanos - lastNanos.longValue() >= periodNanos;
    }

    private static long countFailed(MeasurementValidity validity) {
        if (validity == MeasurementValidity.MISSING || validity == MeasurementValidity.OUT_OF_RANGE) {
            return 1L;
        }
        return 0L;
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
