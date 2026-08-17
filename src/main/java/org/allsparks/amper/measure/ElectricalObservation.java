package org.allsparks.amper.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Aggregated observation for one monitor update cycle. */
public final class ElectricalObservation {
    private final long loopStartNanos;
    private final long loopDurationNanos;
    private final VoltageSample rawVoltage;
    private final VoltageSample filteredVoltage;
    private final double voltageMinimumThisMatch;
    private final CurrentSample batteryCurrent;
    private final List<MotorSnapshot> motors;
    private final List<CurrentSample> motorCurrents;
    private final boolean sensingValid;

    public ElectricalObservation(
            long loopStartNanos,
            long loopDurationNanos,
            VoltageSample rawVoltage,
            VoltageSample filteredVoltage,
            double voltageMinimumThisMatch,
            CurrentSample batteryCurrent,
            List<MotorSnapshot> motors,
            boolean sensingValid) {
        this.loopStartNanos = loopStartNanos;
        this.loopDurationNanos = loopDurationNanos;
        this.rawVoltage = Objects.requireNonNull(rawVoltage, "rawVoltage");
        this.filteredVoltage = Objects.requireNonNull(filteredVoltage, "filteredVoltage");
        this.voltageMinimumThisMatch = voltageMinimumThisMatch;
        this.batteryCurrent = Objects.requireNonNull(batteryCurrent, "batteryCurrent");
        this.motors = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(motors, "motors")));
        List<CurrentSample> currents = new ArrayList<>(this.motors.size());
        for (MotorSnapshot motor : this.motors) {
            currents.add(motor.current());
        }
        this.motorCurrents = Collections.unmodifiableList(currents);
        this.sensingValid = sensingValid;
    }

    public long loopStartNanos() {
        return loopStartNanos;
    }

    public long loopDurationNanos() {
        return loopDurationNanos;
    }

    public VoltageSample rawVoltage() {
        return rawVoltage;
    }

    public VoltageSample filteredVoltage() {
        return filteredVoltage;
    }

    public double voltageMinimumThisMatch() {
        return voltageMinimumThisMatch;
    }

    public CurrentSample batteryCurrent() {
        return batteryCurrent;
    }

    public List<MotorSnapshot> motors() {
        return motors;
    }

    public List<CurrentSample> motorCurrents() {
        return motorCurrents;
    }

    public boolean sensingValid() {
        return sensingValid;
    }

    public double totalAbsCommandedEffort() {
        double sum = 0.0;
        for (MotorSnapshot motor : motors) {
            double effort = motor.commandedEffort();
            if (!Double.isNaN(effort)) {
                sum += Math.abs(effort);
            }
        }
        return sum;
    }
}
