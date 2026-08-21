package org.allsparks.amper.log;

import java.util.Locale;
import java.util.Map;
import org.allsparks.amper.AmperVersion;

/** Sidecar JSON describing AdvantageScope CSV fields, units, and name mapping. */
public final class LogSchemaSidecar {
    private LogSchemaSidecar() {}

    public static String toJson(CanonicalLog log, SessionMetadata metadata) {
        SessionMetadata meta = metadata == null ? SessionMetadata.anonymous("unspecified") : metadata;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        field(sb, "schemaVersion", AmperVersion.LOG_SCHEMA_VERSION);
        field(sb, "libraryVersion", AmperVersion.VERSION);
        field(sb, "format", "advantagescope-csv-table");
        field(sb, "timestampUnit", "seconds");
        field(sb, "internalTimestampUnit", "nanoseconds");
        field(sb, "keyPrefix", LogKeys.PREFIX);
        field(sb, "sessionId", meta.sessionId());
        field(sb, "hardwarePlatform", meta.hardwarePlatform());
        sb.append("  \"csvLayout\": \"table\",\n");
        sb.append("  \"hardwareNameMapping\": {\n");
        appendMapping(sb, log.names().mapping());
        sb.append("  },\n");
        sb.append("  \"fields\": {\n");
        appendFields(sb, log.schema());
        sb.append("  },\n");
        sb.append("  \"terminology\": {\n");
        sb.append("    \"selectedMotorsCurrentAmps\": ");
        sb.append("\"Sum of VALID currents for motors AMPER observes. Not FRC PDH/PDP total current.\",\n");
        sb.append("    \"notClaimed\": [\n");
        sb.append("      \"PDH/PDP branch-current monitoring\",\n");
        sb.append("      \"true robot total-current measurement\",\n");
        sb.append("      \"roboRIO brownout state\",\n");
        sb.append("      \"TalonFX supply-current limiting\",\n");
        sb.append("      \"SystemCore telemetry not documented by FIRST or REV\"\n");
        sb.append("    ]\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static void field(StringBuilder sb, String key, String value) {
        sb.append("  \"")
                .append(Json.escape(key))
                .append("\": \"")
                .append(Json.escape(value))
                .append("\",\n");
    }

    private static void appendMapping(StringBuilder sb, Map<String, String> mapping) {
        int remaining = mapping.size();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            remaining--;
            sb.append("    \"")
                    .append(Json.escape(entry.getKey()))
                    .append("\": \"")
                    .append(Json.escape(entry.getValue()))
                    .append('"');
            if (remaining > 0) {
                sb.append(',');
            }
            sb.append('\n');
        }
    }

    private static void appendFields(StringBuilder sb, Map<String, LogFieldSpec> schema) {
        int remaining = schema.size();
        for (Map.Entry<String, LogFieldSpec> entry : schema.entrySet()) {
            remaining--;
            LogFieldSpec spec = entry.getValue();
            sb.append("    \"").append(Json.escape(entry.getKey())).append("\": {");
            Json.appendString(sb, "type", spec.type().name().toLowerCase(Locale.US));
            sb.append(',');
            String inner = spec.metadataJson();
            if (inner.length() >= 2) {
                sb.append(inner.substring(1, inner.length() - 1));
            }
            sb.append('}');
            if (remaining > 0) {
                sb.append(',');
            }
            sb.append('\n');
        }
    }
}
