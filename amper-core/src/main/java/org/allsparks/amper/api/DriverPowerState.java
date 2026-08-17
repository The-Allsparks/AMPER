package org.allsparks.amper.api;

/**
 * Concise driver-facing electrical awareness states.
 * Rate-limit publishing so telemetry does not degrade the robot loop.
 */
public enum DriverPowerState {
    NORMAL,
    ELEVATED_DEMAND,
    INTERVENTION_ACTIVE,
    SEVERE_VOLTAGE_RISK,
    INVALID_SENSING,
    SUSPECTED_WEAK_BATTERY,
    SUSPECTED_STALLED_MECHANISM
}
