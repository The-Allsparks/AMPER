package org.allsparks.amper.log;

import java.util.Collections;
import java.util.Set;
import org.allsparks.amper.AmperVersion;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.measure.VoltageSample;
import org.allsparks.amper.telemetry.DriverTelemetry;

/**
 * Projects an {@link ElectricalObservation} into the canonical log. Never
 * mutates observation values and never commands hardware.
 */
public final class CanonicalLogPublisher {
    private final CanonicalLog log;
    private final SessionMetadata metadata;
    private long previousLoopStartNanos = Long.MIN_VALUE;
    private boolean metadataRegistered;

    public CanonicalLogPublisher(CanonicalLog log, SessionMetadata metadata) {
        this.log = log;
        this.metadata = metadata == null ? SessionMetadata.anonymous("unspecified") : metadata;
        FieldSpecs.registerSessionMetadata(log);
        this.metadataRegistered = true;
    }

    public CanonicalLog log() {
        return log;
    }

    public void record(
            ElectricalObservation observation,
            DriverTelemetry driver,
            long droppedRecords,
            String eventType,
            String eventMessage,
            Set<String> stalledMotorIds) {
        if (observation == null || observation.disabled()) {
            return;
        }
        ensureRegistered(observation);
        Set<String> stalled = stalledMotorIds == null
                ? Collections.<String>emptySet()
                : stalledMotorIds;
        long now = observation.loopStartNanos();
        CanonicalSample.Builder row = CanonicalSample.at(now);
        row.putString(LogKeys.METADATA_SCHEMA_VERSION, AmperVersion.LOG_SCHEMA_VERSION);
        row.putString(LogKeys.METADATA_LIBRARY_VERSION, AmperVersion.VERSION);
        row.putString(LogKeys.METADATA_SESSION_ID, metadata.sessionId());
        row.putString(LogKeys.METADATA_HARDWARE_PLATFORM, metadata.hardwarePlatform());

        putVoltage(row, LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS, observation.rawVoltage());
        putVoltage(row, LogKeys.SYSTEM_FILTERED_VOLTAGE_VOLTS, observation.filteredVoltage());
        if (!Double.isNaN(observation.voltageMinimumThisMatch())) {
            row.putDouble(LogKeys.SYSTEM_MINIMUM_VOLTAGE_VOLTS, observation.voltageMinimumThisMatch());
        }
        row.putString(LogKeys.SYSTEM_MEASUREMENT_VALIDITY, observation.rawVoltage().validity().name());
        row.putString(
                LogKeys.SYSTEM_POWER_STATE,
                driver == null ? "NORMAL" : driver.state().name());

        for (VoltageSample hub : observation.allVoltages()) {
            String hubKey = log.names().sanitize(hub.sourceId());
            putVoltage(row, LogKeys.hubVoltageVolts(hubKey), hub);
            row.putDouble(LogKeys.hubSampleAgeSeconds(hubKey), hub.ageNanos(now) / 1_000_000_000.0);
        }

        double selectedCurrent = 0.0;
        int selectedCount = 0;
        for (MotorSnapshot motor : observation.motors()) {
            String motorKey = log.names().sanitize(motor.motorId());
            if (!Double.isNaN(motor.commandedEffort())) {
                row.putDouble(LogKeys.motorCommand(motorKey), motor.commandedEffort());
                row.putDouble(LogKeys.motorAppliedCommand(motorKey), motor.commandedEffort());
                row.putDouble(LogKeys.mechanismRequestedEffort(motorKey), motor.commandedEffort());
                row.putDouble(LogKeys.mechanismGrantedEffort(motorKey), motor.commandedEffort());
            }
            row.putBoolean(LogKeys.mechanismConstrained(motorKey), false);
            CurrentSample current = motor.current();
            if (current.isUsable()) {
                row.putDouble(LogKeys.motorCurrentAmps(motorKey), current.amps());
                selectedCurrent += current.amps();
                selectedCount++;
            }
            if (!Double.isNaN(motor.velocityTicksPerSecond())) {
                row.putDouble(LogKeys.motorVelocityTicksPerSecond(motorKey), motor.velocityTicksPerSecond());
            }
            if (current.validity() != MeasurementValidity.UNSUPPORTED
                    && current.validity() != MeasurementValidity.MISSING) {
                row.putDouble(
                        LogKeys.motorCurrentSampleAgeSeconds(motorKey),
                        current.ageNanos(now) / 1_000_000_000.0);
            }
            row.putBoolean(LogKeys.motorStallSuspected(motorKey), stalled.contains(motor.motorId()));
        }
        if (selectedCount > 0) {
            row.putDouble(LogKeys.SYSTEM_SELECTED_MOTORS_CURRENT_AMPS, selectedCurrent);
        }

        row.putDouble(
                LogKeys.PERFORMANCE_UPDATE_DURATION_SECONDS,
                observation.loopDurationNanos() / 1_000_000_000.0);
        if (previousLoopStartNanos != Long.MIN_VALUE && now >= previousLoopStartNanos) {
            row.putDouble(
                    LogKeys.PERFORMANCE_LOOP_DURATION_SECONDS,
                    (now - previousLoopStartNanos) / 1_000_000_000.0);
        }
        row.putInt64(LogKeys.PERFORMANCE_DROPPED_RECORDS, droppedRecords);
        if (eventType != null && !eventType.isEmpty()) {
            row.putString(LogKeys.EVENTS_TYPE, eventType);
        }
        if (eventMessage != null && !eventMessage.isEmpty()) {
            row.putString(LogKeys.EVENTS_MESSAGE, eventMessage);
        }
        previousLoopStartNanos = now;
        log.append(row.build());
    }

    public void reset() {
        previousLoopStartNanos = Long.MIN_VALUE;
        log.clear();
        FieldSpecs.registerSessionMetadata(log);
        metadataRegistered = true;
    }

    private void ensureRegistered(ElectricalObservation observation) {
        if (!metadataRegistered) {
            FieldSpecs.registerSessionMetadata(log);
            metadataRegistered = true;
        }
        for (VoltageSample hub : observation.allVoltages()) {
            String hubKey = log.names().sanitize(hub.sourceId());
            log.register(FieldSpecs.hubVoltage(hubKey));
            log.register(FieldSpecs.hubSampleAge(hubKey));
        }
        for (MotorSnapshot motor : observation.motors()) {
            String motorKey = log.names().sanitize(motor.motorId());
            log.register(FieldSpecs.motorCommand(motorKey));
            log.register(FieldSpecs.motorAppliedCommand(motorKey));
            log.register(FieldSpecs.motorCurrent(motorKey));
            log.register(FieldSpecs.motorVelocity(motorKey));
            log.register(FieldSpecs.motorCurrentAge(motorKey));
            log.register(FieldSpecs.motorStall(motorKey));
            log.register(FieldSpecs.mechanismRequested(motorKey));
            log.register(FieldSpecs.mechanismGranted(motorKey));
            log.register(FieldSpecs.mechanismConstrained(motorKey));
        }
    }

    private static void putVoltage(CanonicalSample.Builder row, String key, VoltageSample sample) {
        if (sample != null && sample.isUsable()) {
            row.putDouble(key, sample.volts());
        }
    }
}
