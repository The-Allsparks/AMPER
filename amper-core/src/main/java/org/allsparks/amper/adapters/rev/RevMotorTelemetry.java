package org.allsparks.amper.adapters.rev;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorElectricalTelemetry;

/**
 * REV motor channel telemetry adapter. Wire {@code currentAmps} to
 * {@code DcMotorEx#getCurrent(CurrentUnit.AMPS)} on-robot.
 *
 * <p>Note (from community references such as Game Manual 0): motor current is
 * typically not included in bulk reads; calling it has communication cost.
 */
public final class RevMotorTelemetry implements MotorElectricalTelemetry {
    private final String motorId;
    private final DoubleSupplier currentAmps;
    private final DoubleSupplier commandedEffort;
    private final DoubleSupplier velocityTicksPerSecond;
    private final DoubleSupplier positionTicks;
    private final boolean currentSupported;

    public RevMotorTelemetry(
            String motorId,
            DoubleSupplier currentAmps,
            DoubleSupplier commandedEffort,
            DoubleSupplier velocityTicksPerSecond,
            DoubleSupplier positionTicks,
            boolean currentSupported) {
        this.motorId = Objects.requireNonNull(motorId, "motorId");
        this.currentAmps = currentAmps;
        this.commandedEffort = Objects.requireNonNull(commandedEffort, "commandedEffort");
        this.velocityTicksPerSecond = velocityTicksPerSecond;
        this.positionTicks = positionTicks;
        this.currentSupported = currentSupported;
    }

    @Override
    public String motorId() {
        return motorId;
    }

    @Override
    public CurrentSample readCurrent(long nowNanos) {
        if (!currentSupported || currentAmps == null) {
            return CurrentSample.unsupported(nowNanos, motorId);
        }
        try {
            double amps = currentAmps.getAsDouble();
            if (Double.isNaN(amps)) {
                return CurrentSample.missing(nowNanos, motorId);
            }
            return new CurrentSample(amps, nowNanos, MeasurementValidity.VALID, motorId);
        } catch (RuntimeException ex) {
            return CurrentSample.missing(nowNanos, motorId);
        }
    }

    @Override
    public double commandedEffort() {
        return commandedEffort.getAsDouble();
    }

    @Override
    public double velocityTicksPerSecond() {
        return velocityTicksPerSecond == null ? Double.NaN : velocityTicksPerSecond.getAsDouble();
    }

    @Override
    public double positionTicks() {
        return positionTicks == null ? Double.NaN : positionTicks.getAsDouble();
    }
}
