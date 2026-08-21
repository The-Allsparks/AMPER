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

    /**
     * Four-decimal US format matching {@code String.format(Locale.US, "%.4f", value)}
     * for finite magnitudes used on the observe path. Avoids allocating a Formatter.
     */
    public static String fixed4(double value) {
        StringBuilder sb = new StringBuilder(16);
        appendFixed4(sb, value);
        return sb.toString();
    }

    public static void appendFixed4(StringBuilder sb, double value) {
        if (Double.isNaN(value)) {
            sb.append("NaN");
            return;
        }
        if (Double.isInfinite(value)) {
            sb.append(value > 0 ? "Infinity" : "-Infinity");
            return;
        }
        if (value >= 1.0e12 || value <= -1.0e12) {
            sb.append(String.format(Locale.US, "%.4f", value));
            return;
        }
        boolean negative = value < 0.0;
        double abs = negative ? -value : value;
        long scaled = Math.round(abs * 10000.0);
        if (negative && scaled != 0L) {
            sb.append('-');
        }
        long whole = scaled / 10000L;
        long frac = scaled % 10000L;
        sb.append(whole).append('.');
        if (frac < 10L) {
            sb.append("000");
        } else if (frac < 100L) {
            sb.append("00");
        } else if (frac < 1000L) {
            sb.append("0");
        }
        sb.append(frac);
    }

    /**
     * Decimal seconds for AdvantageScope CSV. Locale.US only. Internal time stays
     * integer nanoseconds until this boundary.
     */
    public static String secondsFromNanos(long nanos) {
        return String.format(Locale.US, "%.9f", nanos / 1_000_000_000.0);
    }

    public static String booleanLiteral(boolean value) {
        return value ? "true" : "false";
    }

    /** Always-quoted UTF-8 string cell (AdvantageScope string encoding). */
    public static String quoteString(String value) {
        String raw = value == null ? "" : value;
        StringBuilder sb = new StringBuilder(raw.length() + 2);
        sb.append('"');
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
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

    /**
     * Sanitize a file name while keeping a caller-supplied extension
     * ({@code .csv}, {@code .schema.json}, {@code .wpilog}).
     */
    public static String sanitizeLeaf(String raw) {
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
        if (out.indexOf('.') < 0) {
            out = out + ".csv";
        }
        return out;
    }

    public static String sidecarFilename(String csvFilename) {
        String csv = sanitizeFilename(csvFilename);
        String lower = csv.toLowerCase(Locale.US);
        if (lower.endsWith(".csv")) {
            return csv.substring(0, csv.length() - 4) + ".schema.json";
        }
        return csv + ".schema.json";
    }
}
