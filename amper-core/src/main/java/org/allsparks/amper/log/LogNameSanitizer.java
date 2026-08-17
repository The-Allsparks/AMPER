package org.allsparks.amper.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps FTC hardware-map names onto hierarchy-safe log segments.
 *
 * <p>Original names are preserved in {@link #mapping()} so students can correlate
 * Driver Station configuration names with AdvantageScope keys.
 */
public final class LogNameSanitizer {
    private final Map<String, String> originalToSanitized = new LinkedHashMap<String, String>();
    private final Map<String, String> sanitizedToOriginal = new LinkedHashMap<String, String>();

    public String sanitize(String raw) {
        String original = raw == null ? "" : raw;
        String existing = originalToSanitized.get(original);
        if (existing != null) {
            return existing;
        }
        String candidate = sanitizeSegment(original);
        if (sanitizedToOriginal.containsKey(candidate)
                && !original.equals(sanitizedToOriginal.get(candidate))) {
            int suffix = 2;
            String unique = candidate + "_" + suffix;
            while (sanitizedToOriginal.containsKey(unique)) {
                suffix++;
                unique = candidate + "_" + suffix;
            }
            candidate = unique;
        }
        originalToSanitized.put(original, candidate);
        sanitizedToOriginal.put(candidate, original);
        return candidate;
    }

    public Map<String, String> mapping() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(originalToSanitized));
    }

    static String sanitizeSegment(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "unnamed";
        }
        StringBuilder sb = new StringBuilder();
        boolean lastUnderscore = false;
        String trimmed = raw.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9');
            if (ok) {
                sb.append(c);
                lastUnderscore = false;
            } else if (!lastUnderscore) {
                sb.append('_');
                lastUnderscore = true;
            }
        }
        String out = sb.toString();
        while (out.startsWith("_")) {
            out = out.substring(1);
        }
        while (out.endsWith("_")) {
            out = out.substring(0, out.length() - 1);
        }
        if (out.isEmpty() || ".".equals(out) || "..".equals(out)) {
            return "unnamed";
        }
        String lower = out.toLowerCase(Locale.US);
        if ("amper".equals(lower) || "metadata".equals(lower)) {
            return "name_" + out;
        }
        return out;
    }
}
