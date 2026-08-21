package org.allsparks.amper.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private PowerEvent lastAnnotatingEvent;
    private long dropped;
    private boolean exported;

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
        Map<String, String> fields = new LinkedHashMap<String, String>();
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

        int hub = 0;
        for (VoltageSample extra : observation.allVoltages()) {
            fields.put("hub" + hub + "Id", extra.sourceId());
            fields.put("hub" + hub + "V", format(extra.volts()));
            fields.put("hub" + hub + "Validity", extra.validity().name());
            hub++;
        }

        int index = 0;
        for (MotorSnapshot motor : observation.motors()) {
            fields.put("m" + index + "Id", motor.motorId());
            fields.put("m" + index + "A", format(motor.current().amps()));
            fields.put("m" + index + "Validity", motor.current().validity().name());
            fields.put("m" + index + "AgeNs", Long.toString(
                    motor.current().ageNanos(observation.loopStartNanos())));
            fields.put("m" + index + "Cmd", format(motor.commandedEffort()));
            fields.put("m" + index + "Vel", format(motor.velocityTicksPerSecond()));
            fields.put("m" + index + "Active", Boolean.toString(motor.active()));
            fields.put("m" + index + "CurrentRead", Boolean.toString(motor.currentReadThisLoop()));
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

    private static String format(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        return String.format(Locale.US, "%.4f", value);
    }
}
