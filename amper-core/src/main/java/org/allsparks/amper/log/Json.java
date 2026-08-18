package org.allsparks.amper.log;

import java.util.Locale;

/** Minimal locale-independent JSON string builder. No third-party parser. */
public final class Json {
    private Json() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format(Locale.US, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    public static void appendString(StringBuilder sb, String key, String value) {
        sb.append('"').append(escape(key)).append("\":\"").append(escape(value)).append('"');
    }

    public static void appendBoolean(StringBuilder sb, String key, boolean value) {
        sb.append('"').append(escape(key)).append("\":").append(value ? "true" : "false");
    }

    public static void appendRaw(StringBuilder sb, String key, String rawJson) {
        sb.append('"').append(escape(key)).append("\":").append(rawJson);
    }
}
