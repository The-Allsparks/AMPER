package org.allsparks.amper.sim;

import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.measure.VoltageSample;

/** Mutable hub source for simulation. Never commands motors. */
public final class SimulatedHubSource implements PowerTelemetrySource {
    private final String name;
    private double volts = 12.6;
    private MeasurementValidity voltageValidity = MeasurementValidity.VALID;
    private long voltageTimestampOffset;
    private CurrentSample batteryCurrent;

    public SimulatedHubSource(String name) {
        this.name = name == null ? "sim-hub" : name;
        this.batteryCurrent = CurrentSample.unsupported(0L, this.name + ":battery");
    }

    public void setVolts(double volts) {
        this.volts = volts;
        this.voltageValidity = MeasurementValidity.VALID;
        this.voltageTimestampOffset = 0L;
    }

    public void setMissing() {
        this.voltageValidity = MeasurementValidity.MISSING;
        this.volts = Double.NaN;
    }

    public void setStaleOffset(long offsetNanos) {
        this.voltageTimestampOffset = offsetNanos;
    }

    public void setBatteryCurrent(CurrentSample sample) {
        this.batteryCurrent = sample;
    }

    @Override
    public VoltageSample readBusVoltage(long nowNanos) {
        if (voltageValidity != MeasurementValidity.VALID) {
            return new VoltageSample(volts, nowNanos, voltageValidity, name);
        }
        return new VoltageSample(volts, nowNanos - voltageTimestampOffset, MeasurementValidity.VALID, name);
    }

    @Override
    public CurrentSample readBatteryCurrent(long nowNanos) {
        if (batteryCurrent == null) {
            return CurrentSample.unsupported(nowNanos, name + ":battery");
        }
        return new CurrentSample(
                batteryCurrent.amps(), nowNanos, batteryCurrent.validity(), batteryCurrent.channelId());
    }

    @Override
    public String sourceName() {
        return name;
    }
}
