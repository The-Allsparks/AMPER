package org.allsparks.amper.measure;

/**
 * Hardware-independent source of bus / battery electrical telemetry.
 * Implementations must not command motors.
 */
public interface PowerTelemetrySource {
    VoltageSample readBusVoltage(long nowNanos);

    /**
     * Optional hub-level battery current. Return
     * {@link CurrentSample#unsupported(long, String)} when unavailable.
     */
    CurrentSample readBatteryCurrent(long nowNanos);

    String sourceName();
}
