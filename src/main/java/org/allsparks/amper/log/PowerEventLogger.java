package org.allsparks.amper.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.measure.VoltageSample;

/**
 * Records time-correlated electrical events. Exportable for offline analysis.
 * Does not command hardware.
 */
public final class PowerEventLogger {
    private final int capacity;
    private final List<PowerEvent> events;
    private long dropped;

    public PowerEventLogger(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.events = new ArrayList<>(capacity);
    }

    public void record(PowerEvent event) {
        Objects.requireNonNull(event, "event");
        if (events.size() >= capacity) {
            events.remove(0);
            dropped++;
        }
        events.add(event);
    }

    public void recordObservation(ElectricalObservation observation) {
        Objects.requireNonNull(observation, "observation");
        Map<String, String> fields = new LinkedHashMap<>();
        VoltageSample raw = observation.rawVoltage();
        VoltageSample filtered = observation.filteredVoltage();
        fields.put("rawV", format(raw.volts()));
        fields.put("rawValidity", raw.validity().name());
        fields.put("filtV", format(filtered.volts()));
        fields.put("minV", format(observation.voltageMinimumThisMatch()));
        fields.put("battA", format(observation.batteryCurrent().amps()));
        fields.put("battValidity", observation.batteryCurrent().validity().name());
        fields.put("loopNs", Long.toString(observation.loopDurationNanos()));
        fields.put("sensingValid", Boolean.toString(observation.sensingValid()));
        fields.put("sumAbsCmd", format(observation.totalAbsCommandedEffort()));

        int index = 0;
        for (MotorSnapshot motor : observation.motors()) {
            fields.put("m" + index + "Id", motor.motorId());
            fields.put("m" + index + "A", format(motor.current().amps()));
            fields.put("m" + index + "Validity", motor.current().validity().name());
            fields.put("m" + index + "Cmd", format(motor.commandedEffort()));
            fields.put("m" + index + "Vel", format(motor.velocityTicksPerSecond()));
            index++;
        }

        PowerEventType type = observation.sensingValid()
                ? PowerEventType.LOOP_SAMPLE
                : PowerEventType.SENSOR_INVALID;
        record(new PowerEvent(observation.loopStartNanos(), type, "observation", fields));
    }

    public List<PowerEvent> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public long droppedCount() {
        return dropped;
    }

    public void clear() {
        events.clear();
        dropped = 0;
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder();
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
