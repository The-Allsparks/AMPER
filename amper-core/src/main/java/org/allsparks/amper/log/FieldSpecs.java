package org.allsparks.amper.log;

/** Factory for the stable AMPER field catalog. */
public final class FieldSpecs {
    private FieldSpecs() {
    }

    public static final String UNIT_VOLTS = "volts";
    public static final String UNIT_AMPERES = "amperes";
    public static final String UNIT_SECONDS = "seconds";
    public static final String UNIT_TICKS_PER_SECOND = "ticks_per_second";
    public static final String UNIT_NONE = "none";
    public static final String UNIT_EFFORT = "command_effort";

    public static final String VALID_OR_EMPTY =
            "Numeric cell is empty unless MeasurementValidity is VALID. Never encoded as 0 or NaN.";

    public static LogFieldSpec string(String key, String source, String cadence) {
        return new LogFieldSpec(
                key, LogValueType.STRING, UNIT_NONE, source, false, false, cadence, "AMPER", "");
    }

    public static LogFieldSpec metadataString(String key) {
        return string(key, "AMPER session metadata", "session");
    }

    public static LogFieldSpec busVoltage() {
        return new LogFieldSpec(
                LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS,
                LogValueType.DOUBLE,
                UNIT_VOLTS,
                "FTC VoltageSensor.getVoltage",
                true,
                false,
                "every-loop",
                "policy-voltage-source",
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec filteredVoltage() {
        return new LogFieldSpec(
                LogKeys.SYSTEM_FILTERED_VOLTAGE_VOLTS,
                LogValueType.DOUBLE,
                UNIT_VOLTS,
                "AMPER low-pass of measured bus voltage",
                true,
                false,
                "every-loop",
                "policy-voltage-source",
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec minimumVoltage() {
        return new LogFieldSpec(
                LogKeys.SYSTEM_MINIMUM_VOLTAGE_VOLTS,
                LogValueType.DOUBLE,
                UNIT_VOLTS,
                "AMPER match minimum of valid measured bus voltage",
                true,
                false,
                "every-loop",
                "policy-voltage-source",
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec measurementValidity() {
        return string(LogKeys.SYSTEM_MEASUREMENT_VALIDITY, "AMPER validity classifier", "every-loop");
    }

    public static LogFieldSpec powerState() {
        return string(LogKeys.SYSTEM_POWER_STATE, "AMPER driver-facing PowerState", "every-loop");
    }

    public static LogFieldSpec selectedMotorsCurrent() {
        return new LogFieldSpec(
                LogKeys.SYSTEM_SELECTED_MOTORS_CURRENT_AMPS,
                LogValueType.DOUBLE,
                UNIT_AMPERES,
                "Sum of VALID currents for motors AMPER is observing. Not robot total current.",
                true,
                false,
                "when-valid-currents-exist",
                "selected-DcMotorEx",
                "Empty when no observed motor has VALID current this sample. Not TotalCurrentAmps.");
    }

    public static LogFieldSpec hubVoltage(String hub) {
        return new LogFieldSpec(
                LogKeys.hubVoltageVolts(hub),
                LogValueType.DOUBLE,
                UNIT_VOLTS,
                "FTC VoltageSensor.getVoltage",
                true,
                false,
                "every-loop",
                hub,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec hubSampleAge(String hub) {
        return new LogFieldSpec(
                LogKeys.hubSampleAgeSeconds(hub),
                LogValueType.DOUBLE,
                UNIT_SECONDS,
                "now minus voltage capture time",
                true,
                false,
                "every-loop",
                hub,
                "Always seconds. Empty if capture time is unknown.");
    }

    public static LogFieldSpec motorCommand(String motor) {
        return new LogFieldSpec(
                LogKeys.motorCommand(motor),
                LogValueType.DOUBLE,
                UNIT_EFFORT,
                "DcMotorEx.getPower observed, never written by AMPER",
                true,
                false,
                "every-loop",
                motor,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec motorAppliedCommand(String motor) {
        return new LogFieldSpec(
                LogKeys.motorAppliedCommand(motor),
                LogValueType.DOUBLE,
                UNIT_EFFORT,
                "Phase 0/1 identity: equals Command because AMPER does not modify output",
                true,
                false,
                "every-loop",
                motor,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec motorCurrent(String motor) {
        return new LogFieldSpec(
                LogKeys.motorCurrentAmps(motor),
                LogValueType.DOUBLE,
                UNIT_AMPERES,
                "DcMotorEx.getCurrent(CurrentUnit.AMPS) when the SDK exposes it",
                true,
                false,
                "round-robin-or-every-loop",
                motor,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec motorVelocity(String motor) {
        return new LogFieldSpec(
                LogKeys.motorVelocityTicksPerSecond(motor),
                LogValueType.DOUBLE,
                UNIT_TICKS_PER_SECOND,
                "DcMotorEx.getVelocity encoder ticks; not converted to rotations",
                true,
                false,
                "every-loop",
                motor,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec motorCurrentAge(String motor) {
        return new LogFieldSpec(
                LogKeys.motorCurrentSampleAgeSeconds(motor),
                LogValueType.DOUBLE,
                UNIT_SECONDS,
                "now minus current capture time",
                true,
                false,
                "every-loop",
                motor,
                "Seconds. Empty if no current capture exists.");
    }

    public static LogFieldSpec motorStall(String motor) {
        return new LogFieldSpec(
                LogKeys.motorStallSuspected(motor),
                LogValueType.BOOLEAN,
                UNIT_NONE,
                "AMPER stall-suspicion tracker",
                false,
                true,
                "every-loop",
                motor,
                "true/false. Warning only; not a hardware stall bit.");
    }

    public static LogFieldSpec mechanismRequested(String mechanism) {
        return new LogFieldSpec(
                LogKeys.mechanismRequestedEffort(mechanism),
                LogValueType.DOUBLE,
                UNIT_EFFORT,
                "Observed commanded effort for this mechanism name",
                true,
                false,
                "every-loop",
                mechanism,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec mechanismGranted(String mechanism) {
        return new LogFieldSpec(
                LogKeys.mechanismGrantedEffort(mechanism),
                LogValueType.DOUBLE,
                UNIT_EFFORT,
                "Phase 0/1 identity grant (no allocator)",
                false,
                false,
                "every-loop",
                mechanism,
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec mechanismConstrained(String mechanism) {
        return new LogFieldSpec(
                LogKeys.mechanismConstrained(mechanism),
                LogValueType.BOOLEAN,
                UNIT_NONE,
                "True only when an enabled coordinator reduced the grant",
                false,
                false,
                "every-loop",
                mechanism,
                "true/false. Phase 0/1 is always false.");
    }

    public static LogFieldSpec updateDuration() {
        return new LogFieldSpec(
                LogKeys.PERFORMANCE_UPDATE_DURATION_SECONDS,
                LogValueType.DOUBLE,
                UNIT_SECONDS,
                "AMPER observe() duration",
                true,
                false,
                "every-loop",
                "AMPER",
                VALID_OR_EMPTY);
    }

    public static LogFieldSpec loopDuration() {
        return new LogFieldSpec(
                LogKeys.PERFORMANCE_LOOP_DURATION_SECONDS,
                LogValueType.DOUBLE,
                UNIT_SECONDS,
                "Elapsed time between observe() calls",
                true,
                false,
                "every-loop",
                "AMPER",
                "Empty on the first sample of a session.");
    }

    public static LogFieldSpec droppedRecords() {
        return new LogFieldSpec(
                LogKeys.PERFORMANCE_DROPPED_RECORDS,
                LogValueType.INT64,
                UNIT_NONE,
                "Count of bounded-log overwrites",
                false,
                false,
                "every-loop",
                "AMPER",
                "");
    }

    public static LogFieldSpec eventType() {
        return string(LogKeys.EVENTS_TYPE, "AMPER PowerEventType", "on-event");
    }

    public static LogFieldSpec eventMessage() {
        return string(LogKeys.EVENTS_MESSAGE, "AMPER event message", "on-event");
    }

    public static void registerSessionMetadata(CanonicalLog log) {
        log.register(metadataString(LogKeys.METADATA_SCHEMA_VERSION));
        log.register(metadataString(LogKeys.METADATA_LIBRARY_VERSION));
        log.register(metadataString(LogKeys.METADATA_SESSION_ID));
        log.register(metadataString(LogKeys.METADATA_HARDWARE_PLATFORM));
        log.register(busVoltage());
        log.register(filteredVoltage());
        log.register(minimumVoltage());
        log.register(measurementValidity());
        log.register(powerState());
        log.register(selectedMotorsCurrent());
        log.register(updateDuration());
        log.register(loopDuration());
        log.register(droppedRecords());
        log.register(eventType());
        log.register(eventMessage());
    }
}
