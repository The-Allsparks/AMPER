package org.allsparks.amper.log;

import java.util.Locale;

/**
 * Locale-independent CSV helpers. Numbers always use {@link Locale#US}.
 */
public final class CsvFormat {
    private CsvFormat() {
    }

    public static String number(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        return String.format(Locale.US, "%.6f", value);
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ',' || c == '"' || c == '\n' || c == '\r') {
                quote = true;
                break;
            }
        }
        if (!quote) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                sb.append('"').append('"');
            } else if (c == '\n' || c == '\r') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String sanitizeFilename(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "amper-session.csv";
        }
        StringBuilder sb = new StringBuilder();
        String trimmed = raw.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '.'
                    || c == '-'
                    || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        String out = sb.toString();
        if (out.startsWith(".")) {
            out = "amper" + out;
        }
        if (!out.toLowerCase(Locale.US).endsWith(".csv")) {
            out = out + ".csv";
        }
        return out;
    }
}
