package org.allsparks.amper.api;

/** Why an applied effort differs from a requested effort. */
public enum PowerLimitReason {
    NONE,
    FEATURE_DISABLED,
    SENSOR_FAULT,
    LOCAL_SLEW_LIMIT,
    LOCAL_OUTPUT_CAP,
    STALL_PROTECTION,
    VOLTAGE_WATCH,
    VOLTAGE_LIMITING,
    VOLTAGE_CRITICAL,
    COORDINATOR_BUDGET,
    PREDICTION_SHADOW,
    PREDICTION_SHAPING,
    POLICY_FALLBACK
}
