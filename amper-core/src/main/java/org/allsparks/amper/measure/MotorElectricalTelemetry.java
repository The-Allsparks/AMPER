package org.allsparks.amper.measure;

/**
 * Per-motor electrical and command telemetry. Implementations must not set
 * motor power from this interface.
 */
public interface MotorElectricalTelemetry {
    String motorId();

    CurrentSample readCurrent(long nowNanos);

    /** Commanded output in the team's effort units, typically {@code [-1, 1]}. */
    double commandedEffort();

    /** Encoder velocity when available; {@link Double#NaN} if unsupported. */
    double velocityTicksPerSecond();

    /** Encoder position when available; {@link Double#NaN} if unsupported. */
    double positionTicks();
}
