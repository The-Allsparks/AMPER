package org.allsparks.amper.sim;

import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorElectricalTelemetry;

/** Mutable motor telemetry for simulation. Has no setPower / setVelocity. */
public final class SimulatedMotor implements MotorElectricalTelemetry {
    private final String id;
    private double amps = Double.NaN;
    private boolean currentSupported;
    private boolean failCurrent;
    private double command;
    private double velocity;
    private double position;

    public SimulatedMotor(String id) {
        this.id = id == null ? "motor" : id;
    }

    public void setCurrentAmps(double amps) {
        this.amps = amps;
        this.currentSupported = true;
        this.failCurrent = false;
    }

    public void setCurrentUnsupported() {
        this.currentSupported = false;
        this.failCurrent = false;
    }

    public void failCurrentReads() {
        this.failCurrent = true;
    }

    public void setCommand(double command) {
        this.command = command;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    @Override
    public String motorId() {
        return id;
    }

    @Override
    public CurrentSample readCurrent(long nowNanos) {
        if (failCurrent) {
            throw new RuntimeException("simulated current read failure");
        }
        if (!currentSupported) {
            return CurrentSample.unsupported(nowNanos, id);
        }
        if (Double.isNaN(amps)) {
            return CurrentSample.missing(nowNanos, id);
        }
        return new CurrentSample(amps, nowNanos, MeasurementValidity.VALID, id);
    }

    @Override
    public double commandedEffort() {
        return command;
    }

    @Override
    public double velocityTicksPerSecond() {
        return velocity;
    }

    @Override
    public double positionTicks() {
        return position;
    }
}
