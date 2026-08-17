package org.allsparks.amper.log;

/** Stable hierarchical keys under {@code /AMPER}. */
public final class LogKeys {
    public static final String PREFIX = "/AMPER";

    public static final String METADATA_SCHEMA_VERSION = PREFIX + "/Metadata/SchemaVersion";
    public static final String METADATA_LIBRARY_VERSION = PREFIX + "/Metadata/LibraryVersion";
    public static final String METADATA_SESSION_ID = PREFIX + "/Metadata/SessionId";
    public static final String METADATA_HARDWARE_PLATFORM = PREFIX + "/Metadata/HardwarePlatform";

    public static final String SYSTEM_BUS_VOLTAGE_VOLTS = PREFIX + "/System/BusVoltageVolts";
    public static final String SYSTEM_FILTERED_VOLTAGE_VOLTS = PREFIX + "/System/FilteredVoltageVolts";
    public static final String SYSTEM_MINIMUM_VOLTAGE_VOLTS = PREFIX + "/System/MinimumVoltageVolts";
    public static final String SYSTEM_MEASUREMENT_VALIDITY = PREFIX + "/System/MeasurementValidity";
    public static final String SYSTEM_POWER_STATE = PREFIX + "/System/PowerState";
    public static final String SYSTEM_SELECTED_MOTORS_CURRENT_AMPS =
            PREFIX + "/System/SelectedMotorsCurrentAmps";

    public static final String PERFORMANCE_UPDATE_DURATION_SECONDS =
            PREFIX + "/Performance/UpdateDurationSeconds";
    public static final String PERFORMANCE_LOOP_DURATION_SECONDS =
            PREFIX + "/Performance/LoopDurationSeconds";
    public static final String PERFORMANCE_DROPPED_RECORDS = PREFIX + "/Performance/DroppedRecords";

    public static final String EVENTS_TYPE = PREFIX + "/Events/Type";
    public static final String EVENTS_MESSAGE = PREFIX + "/Events/Message";

    private LogKeys() {
    }

    public static String hubVoltageVolts(String hub) {
        return PREFIX + "/Hubs/" + hub + "/VoltageVolts";
    }

    public static String hubSampleAgeSeconds(String hub) {
        return PREFIX + "/Hubs/" + hub + "/SampleAgeSeconds";
    }

    public static String motorCommand(String motor) {
        return PREFIX + "/Motors/" + motor + "/Command";
    }

    public static String motorAppliedCommand(String motor) {
        return PREFIX + "/Motors/" + motor + "/AppliedCommand";
    }

    public static String motorCurrentAmps(String motor) {
        return PREFIX + "/Motors/" + motor + "/CurrentAmps";
    }

    public static String motorVelocityTicksPerSecond(String motor) {
        return PREFIX + "/Motors/" + motor + "/VelocityTicksPerSecond";
    }

    public static String motorCurrentSampleAgeSeconds(String motor) {
        return PREFIX + "/Motors/" + motor + "/CurrentSampleAgeSeconds";
    }

    public static String motorStallSuspected(String motor) {
        return PREFIX + "/Motors/" + motor + "/StallSuspected";
    }

    public static String mechanismRequestedEffort(String mechanism) {
        return PREFIX + "/Mechanisms/" + mechanism + "/RequestedEffort";
    }

    public static String mechanismGrantedEffort(String mechanism) {
        return PREFIX + "/Mechanisms/" + mechanism + "/GrantedEffort";
    }

    public static String mechanismConstrained(String mechanism) {
        return PREFIX + "/Mechanisms/" + mechanism + "/Constrained";
    }
}
