package org.allsparks.amper.log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdvantageScope-compatible CSV (table and list layouts).
 *
 * <p>Timestamps in this CSV are decimal seconds. Internal AMPER time remains
 * integer nanoseconds; conversion happens only here.
 *
 * <p>Layout rules follow Mechanical Advantage AdvantageScope log-file docs
 * (https://docs.advantagescope.org/overview/log-files/ accessed 2026-08-17): header {@code Timestamp} plus keys, booleans
 * {@code true}/{@code false}, quoted strings, empty numeric cells when missing.
 */
public final class AdvantageScopeCsv {
    public static final String TIMESTAMP_HEADER = "Timestamp";
    public static final String KEY_HEADER = "Key";
    public static final String VALUE_HEADER = "Value";

    private AdvantageScopeCsv() {}

    public static String table(CanonicalLog log) {
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<String>(log.schema().keySet());
        sb.append(TIMESTAMP_HEADER);
        for (int i = 0; i < keys.size(); i++) {
            sb.append(',').append(keys.get(i));
        }
        sb.append('\n');
        long origin = log.originNanos();
        for (CanonicalSample sample : log.samples()) {
            long relative = sample.timestampNanos() - origin;
            sb.append(CsvFormat.secondsFromNanos(relative));
            for (int i = 0; i < keys.size(); i++) {
                sb.append(',');
                LogFieldSpec spec = log.schema().get(keys.get(i));
                LogValue value = sample.get(keys.get(i));
                sb.append(formatCell(spec == null ? LogValueType.STRING : spec.type(), value));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String list(CanonicalLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append(TIMESTAMP_HEADER)
                .append(',')
                .append(KEY_HEADER)
                .append(',')
                .append(VALUE_HEADER)
                .append('\n');
        long origin = log.originNanos();
        for (CanonicalSample sample : log.samples()) {
            long relative = sample.timestampNanos() - origin;
            String timestamp = CsvFormat.secondsFromNanos(relative);
            for (Map.Entry<String, LogValue> entry : sample.values().entrySet()) {
                LogValue value = entry.getValue();
                if (value == null || !value.present()) {
                    continue;
                }
                sb.append(timestamp)
                        .append(',')
                        .append(entry.getKey())
                        .append(',')
                        .append(formatCell(value.type(), value))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    public static ParsedTable parseTable(String csv) {
        ParsedTable parsed = new ParsedTable();
        if (csv == null || csv.trim().isEmpty()) {
            return parsed;
        }
        List<String> lines = splitLines(csv);
        if (lines.isEmpty()) {
            return parsed;
        }
        List<String> header = parseRow(lines.get(0));
        if (header.isEmpty() || !TIMESTAMP_HEADER.equals(header.get(0))) {
            throw new IllegalArgumentException(
                    "AdvantageScope table CSV must start with header '" + TIMESTAMP_HEADER + "'");
        }
        parsed.keys.addAll(header.subList(1, header.size()));
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).trim().isEmpty()) {
                continue;
            }
            List<String> cells = parseRow(lines.get(line));
            if (cells.isEmpty()) {
                continue;
            }
            ParsedRow row = new ParsedRow();
            row.timestampSeconds = cells.get(0);
            for (int i = 1; i < header.size(); i++) {
                String key = header.get(i);
                String cell = i < cells.size() ? cells.get(i) : "";
                row.cells.put(key, cell);
            }
            parsed.rows.add(row);
        }
        return parsed;
    }

    static String formatCell(LogValueType type, LogValue value) {
        if (value == null || !value.present()) {
            return "";
        }
        switch (type) {
            case DOUBLE:
                return CsvFormat.number(value.asDouble());
            case BOOLEAN:
                return value.asBoolean() ? "true" : "false";
            case INT64:
                return Long.toString(value.asInt64());
            case STRING:
            default:
                return CsvFormat.quoteString(value.asString());
        }
    }

    static List<String> splitLines(String csv) {
        List<String> lines = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (!inQuotes && (c == '\n' || c == '\r')) {
                if (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                    i++;
                }
                lines.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    static List<String> parseRow(String line) {
        List<String> cells = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    public static final class ParsedTable {
        public final List<String> keys = new ArrayList<String>();
        public final List<ParsedRow> rows = new ArrayList<ParsedRow>();
    }

    public static final class ParsedRow {
        public String timestampSeconds = "";
        public final Map<String, String> cells = new LinkedHashMap<String, String>();

        public double timestampAsSeconds() {
            try {
                return Double.parseDouble(timestampSeconds);
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }

        public long timestampAsMicros() {
            double seconds = timestampAsSeconds();
            if (Double.isNaN(seconds)) {
                return 0L;
            }
            return Math.round(seconds * 1_000_000.0);
        }
    }
}
