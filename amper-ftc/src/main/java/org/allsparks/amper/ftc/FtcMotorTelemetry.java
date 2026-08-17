package org.allsparks.amper.ftc;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import java.util.Objects;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * Observation-only FTC motor adapter. Never calls {@code setPower} or
 * {@code setVelocity}. Command is read via {@link DcMotorEx#getPower()} unless
 * a custom command supplier is provided (so AMPER does not take motor ownership).
 */
public final class FtcMotorTelemetry implements MotorElectricalTelemetry {
    private final String motorId;
    private final DcMotorEx motor;
    private final CommandSource commandSource;
    private final boolean currentSupported;

    public FtcMotorTelemetry(String motorId, DcMotorEx motor) {
        this(motorId, motor, null, true);
    }

    public FtcMotorTelemetry(String motorId, DcMotorEx motor, CommandSource commandSource, boolean currentSupported) {
        this.motorId = Objects.requireNonNull(motorId, "motorId");
        this.motor = Objects.requireNonNull(motor, "motor");
        this.commandSource = commandSource;
        this.currentSupported = currentSupported;
    }

    @Override
    public String motorId() {
        return motorId;
    }

    @Override
    public CurrentSample readCurrent(long nowNanos) {
        if (!currentSupported) {
            return CurrentSample.unsupported(nowNanos, motorId);
        }
        try {
            double amps = motor.getCurrent(CurrentUnit.AMPS);
            if (Double.isNaN(amps)) {
                return CurrentSample.missing(nowNanos, motorId);
            }
            return new CurrentSample(amps, nowNanos, MeasurementValidity.VALID, motorId);
        } catch (UnsupportedOperationException ex) {
            return CurrentSample.unsupported(nowNanos, motorId);
        } catch (RuntimeException ex) {
            return CurrentSample.missing(nowNanos, motorId);
        }
    }

    @Override
    public double commandedEffort() {
        if (commandSource != null) {
            return commandSource.commandedEffort();
        }
        try {
            return motor.getPower();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    @Override
    public double velocityTicksPerSecond() {
        try {
            return motor.getVelocity();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    @Override
    public double positionTicks() {
        try {
            return motor.getCurrentPosition();
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    /** Optional command observation that does not require AMPER to own the motor. */
    public interface CommandSource {
        double commandedEffort();
    }
}
