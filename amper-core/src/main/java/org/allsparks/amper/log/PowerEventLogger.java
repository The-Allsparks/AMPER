package org.allsparks.amper.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.allsparks.amper.AmperVersion;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.measure.VoltageSample;

/**
 * Bounded, time-correlated electrical event log. Exportable for offline analysis.
 * Does not command hardware. Does not write files during {@link #recordObservation}.
 */
public final class PowerEventLogger {
    private final int capacity;
    private final List<PowerEvent> events;
    private final SessionMetadata metadata;
    private final Map<String, String> fieldScratch = new LinkedHashMap<String, String>();
    private final StringBuilder numberBuf = new StringBuilder(24);
    private PowerEvent lastAnnotatingEvent;
    private long dropped;
    private boolean exported;
    private String[] hubIdKeys = new String[0];
    private String[] hubVKeys = new String[0];
    private String[] hubValidityKeys = new String[0];
    private String[] motorIdKeys = new String[0];
    private String[] motorAmpsKeys = new String[0];
    private String[] motorValidityKeys = new String[0];
    private String[] motorAgeKeys = new String[0];
    private String[] motorCmdKeys = new String[0];
    private String[] motorVelKeys = new String[0];
    private String[] motorActiveKeys = new String[0];
    private String[] motorCurrentReadKeys = new String[0];

    public PowerEventLogger(int capacity) {
        this(capacity, SessionMetadata.anonymous("unspecified"));
    }

    public PowerEventLogger(int capacity, SessionMetadata metadata) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.events = new ArrayList<PowerEvent>(capacity);
        this.metadata = metadata == null ? SessionMetadata.anonymous("unspecified") : metadata;
    }

    public void record(PowerEvent event) {
        Objects.requireNonNull(event, "event");
        if (events.size() >= capacity) {
            events.remove(0);
            dropped++;
        }
        events.add(event);
        if (annotatesCanonical(event.type())) {
            lastAnnotatingEvent = event;
        }
    }

    public void recordObservation(ElectricalObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Map<String, String> fields = fieldScratch;
        fields.clear();
        VoltageSample raw = observation.rawVoltage();
        VoltageSample filtered = observation.filteredVoltage();
        fields.put("schema", AmperVersion.CSV_SCHEMA_VERSION);
        fields.put("sourceId", raw.sourceId());
        fields.put("rawV", format(raw.volts()));
        fields.put("rawValidity", raw.validity().name());
        fields.put("rawAgeNs", Long.toString(raw.ageNanos(observation.loopStartNanos())));
        fields.put("filtV", format(filtered.volts()));
        fields.put("filtValidity", filtered.validity().name());
        fields.put("minV", format(observation.voltageMinimumThisMatch()));
        fields.put("battA", format(observation.batteryCurrent().amps()));
        fields.put("battValidity", observation.batteryCurrent().validity().name());
        fields.put("loopNs", Long.toString(observation.loopDurationNanos()));
        fields.put("amperNs", Long.toString(observation.loopDurationNanos()));
        fields.put("sensingValid", Boolean.toString(observation.sensingValid()));
        fields.put("disabled", Boolean.toString(observation.disabled()));
        fields.put("sumAbsCmd", format(observation.totalAbsCommandedEffort()));
        fields.put("dropped", Long.toString(dropped));
        fields.put("failed", Long.toString(observation.samplingStats().failedThisLoop()));
        fields.put("stale", Long.toString(observation.samplingStats().staleThisLoop()));
        fields.put("unsupported", Long.toString(observation.samplingStats().unsupportedThisLoop()));
        fields.put("skipped", Long.toString(observation.samplingStats().skippedThisLoop()));
        fields.put("currentReads", Integer.toString(observation.samplingStats().currentReadsThisLoop()));
        fields.put("hubCount", Integer.toString(observation.allVoltages().size()));

        ensureHubKeys(observation.allVoltages().size());
        int hub = 0;
        for (VoltageSample extra : observation.allVoltages()) {
            fields.put(hubIdKeys[hub], extra.sourceId());
            fields.put(hubVKeys[hub], format(extra.volts()));
            fields.put(hubValidityKeys[hub], extra.validity().name());
            hub++;
        }

        ensureMotorKeys(observation.motors().size());
        int index = 0;
        for (MotorSnapshot motor : observation.motors()) {
            fields.put(motorIdKeys[index], motor.motorId());
            fields.put(motorAmpsKeys[index], format(motor.current().amps()));
            fields.put(motorValidityKeys[index], motor.current().validity().name());
            fields.put(motorAgeKeys[index], Long.toString(
                    motor.current().ageNanos(observation.loopStartNanos())));
            fields.put(motorCmdKeys[index], format(motor.commandedEffort()));
            fields.put(motorVelKeys[index], format(motor.velocityTicksPerSecond()));
            fields.put(motorActiveKeys[index], Boolean.toString(motor.active()));
            fields.put(motorCurrentReadKeys[index], Boolean.toString(motor.currentReadThisLoop()));
            index++;
        }

        PowerEventType type = observation.sensingValid()
                ? PowerEventType.LOOP_SAMPLE
                : PowerEventType.SENSOR_INVALID;
        record(new PowerEvent(observation.loopStartNanos(), type, "observation", fields));
    }

    public List<PowerEvent> snapshot() {
        return Collections.unmodifiableList(new ArrayList<PowerEvent>(events));
    }

    /**
     * Most recent event that may annotate a canonical row. Ignores
     * {@link PowerEventType#LOOP_SAMPLE} and {@link PowerEventType#SENSOR_INVALID}.
     */
    public PowerEvent lastAnnotatingEvent() {
        return lastAnnotatingEvent;
    }

    public long droppedCount() {
        return dropped;
    }

    public int capacity() {
        return capacity;
    }

    public SessionMetadata metadata() {
        return metadata;
    }

    public boolean exported() {
        return exported;
    }

    public void markExported() {
        exported = true;
    }

    public void clear() {
        events.clear();
        lastAnnotatingEvent = null;
        dropped = 0;
        exported = false;
    }

    private static boolean annotatesCanonical(PowerEventType type) {
        return type != PowerEventType.LOOP_SAMPLE && type != PowerEventType.SENSOR_INVALID;
    }

    private void ensureHubKeys(int count) {
        if (hubIdKeys.length >= count) {
            return;
        }
        hubIdKeys = new String[count];
        hubVKeys = new String[count];
        hubValidityKeys = new String[count];
        for (int i = 0; i < count; i++) {
            hubIdKeys[i] = "hub" + i + "Id";
            hubVKeys[i] = "hub" + i + "V";
            hubValidityKeys[i] = "hub" + i + "Validity";
        }
    }

    private void ensureMotorKeys(int count) {
        if (motorIdKeys.length >= count) {
            return;
        }
        motorIdKeys = new String[count];
        motorAmpsKeys = new String[count];
        motorValidityKeys = new String[count];
        motorAgeKeys = new String[count];
        motorCmdKeys = new String[count];
        motorVelKeys = new String[count];
        motorActiveKeys = new String[count];
        motorCurrentReadKeys = new String[count];
        for (int i = 0; i < count; i++) {
            motorIdKeys[i] = "m" + i + "Id";
            motorAmpsKeys[i] = "m" + i + "A";
            motorValidityKeys[i] = "m" + i + "Validity";
            motorAgeKeys[i] = "m" + i + "AgeNs";
            motorCmdKeys[i] = "m" + i + "Cmd";
            motorVelKeys[i] = "m" + i + "Vel";
            motorActiveKeys[i] = "m" + i + "Active";
            motorCurrentReadKeys[i] = "m" + i + "CurrentRead";
        }
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("# amper_csv_schema=").append(AmperVersion.CSV_SCHEMA_VERSION).append('\n');
        sb.append("# amper_version=").append(CsvFormat.escape(AmperVersion.VERSION)).append('\n');
        sb.append("# session_id=").append(CsvFormat.escape(metadata.sessionId())).append('\n');
        sb.append("# policy_note=").append(CsvFormat.escape(metadata.policyNote())).append('\n');
        sb.append("# robot_note=").append(CsvFormat.escape(metadata.robotNote())).append('\n');
        sb.append("# dropped_count=").append(dropped).append('\n');
        sb.append("# pii_policy=no-personal-information\n");
        sb.append("timestampNanos,type,message,fields\n");
        for (PowerEvent event : events) {
            sb.append(event.toExportLine()).append('\n');
        }
        return sb.toString();
    }

    private String format(double value) {
        numberBuf.setLength(0);
        CsvFormat.appendFixed4(numberBuf, value);
        return numberBuf.toString();
    }
}
