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
    private final List<CurrentSample> motorCurrents;
    private final boolean sensingValid;

    public ElectricalObservation(
            long loopStartNanos,
            long loopDurationNanos,
            VoltageSample rawVoltage,
            VoltageSample filteredVoltage,
            double voltageMinimumThisMatch,
            CurrentSample batteryCurrent,
            List<CurrentSample> motorCurrents,
            boolean sensingValid) {
        this.loopStartNanos = loopStartNanos;
        this.loopDurationNanos = loopDurationNanos;
        this.rawVoltage = Objects.requireNonNull(rawVoltage, "rawVoltage");
        this.filteredVoltage = Objects.requireNonNull(filteredVoltage, "filteredVoltage");
        this.voltageMinimumThisMatch = voltageMinimumThisMatch;
        this.batteryCurrent = Objects.requireNonNull(batteryCurrent, "batteryCurrent");
        this.motorCurrents = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(motorCurrents, "motorCurrents")));
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

    public List<CurrentSample> motorCurrents() {
        return motorCurrents;
    }

    public boolean sensingValid() {
        return sensingValid;
    }
}
