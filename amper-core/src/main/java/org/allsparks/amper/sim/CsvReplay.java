package org.allsparks.amper.sim;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventType;

/**
 * Parses AMPER schema-1 CSV (comment header + timestamp,type,message,fields).
 * Used by desktop analysis and software replay tests. Not hardware validation.
 */
public final class CsvReplay {
    private CsvReplay() {
    }

    public static List<PowerEvent> parse(String csv) {
        List<PowerEvent> events = new ArrayList<PowerEvent>();
        if (csv == null || csv.isEmpty()) {
            return events;
        }
        String[] lines = csv.split("\n", -1);
        boolean headerSeen = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (!headerSeen) {
                headerSeen = true;
                continue;
            }
            PowerEvent event = parseLine(line);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    public static List<PowerEvent> loopSamples(String csv) {
        List<PowerEvent> out = new ArrayList<PowerEvent>();
        List<PowerEvent> all = parse(csv);
        for (PowerEvent event : all) {
            if (event.type() == PowerEventType.LOOP_SAMPLE || event.type() == PowerEventType.SENSOR_INVALID) {
                out.add(event);
            }
        }
        return out;
    }

    private static PowerEvent parseLine(String line) {
        List<String> cols = splitCsv(line);
        if (cols.size() < 3) {
            return null;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(cols.get(0).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        PowerEventType type;
        try {
            type = PowerEventType.valueOf(cols.get(1).trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String message = cols.size() > 2 ? cols.get(2) : "";
        String fieldPart = cols.size() > 3 ? cols.get(3) : "";
        Map<String, String> fields = new LinkedHashMap<String, String>();
        if (!fieldPart.isEmpty()) {
            String[] pairs = fieldPart.split(";");
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                int eq = pair.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                fields.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return new PowerEvent(timestamp, type, message, fields);
    }

    static List<String> splitCsv(String line) {
        List<String> cols = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());
        return cols;
    }
}
