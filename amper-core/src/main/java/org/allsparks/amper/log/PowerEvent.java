package org.allsparks.amper.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable time-correlated power event for offline analysis. */
public final class PowerEvent {
    private final long timestampNanos;
    private final PowerEventType type;
    private final String message;
    private final Map<String, String> fields;

    public PowerEvent(long timestampNanos, PowerEventType type, String message, Map<String, String> fields) {
        this.timestampNanos = timestampNanos;
        this.type = Objects.requireNonNull(type, "type");
        this.message = message == null ? "" : message;
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>(
                fields == null ? Collections.<String, String>emptyMap() : fields));
    }

    public long timestampNanos() {
        return timestampNanos;
    }

    public PowerEventType type() {
        return type;
    }

    public String message() {
        return message;
    }

    public Map<String, String> fields() {
        return fields;
    }

    /** CSV-friendly single line (timestamp,type,message,k=v;...). */
    public String toExportLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestampNanos).append(',').append(type.name()).append(',');
        sb.append(CsvFormat.escape(message)).append(',');
        boolean first = true;
        StringBuilder fieldPart = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!first) {
                fieldPart.append(';');
            }
            first = false;
            fieldPart.append(entry.getKey()).append('=').append(entry.getValue() == null ? "" : entry.getValue());
        }
        sb.append(CsvFormat.escape(fieldPart.toString()));
        return sb.toString();
    }
}
