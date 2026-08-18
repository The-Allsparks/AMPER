package org.allsparks.amper.log;

import java.util.Objects;

/** Schema for one hierarchical AMPER field. */
public final class LogFieldSpec {
    private final String key;
    private final LogValueType type;
    private final String unit;
    private final String source;
    private final boolean measured;
    private final boolean estimated;
    private final String cadence;
    private final String hardwareSource;
    private final String validitySemantics;

    public LogFieldSpec(
            String key,
            LogValueType type,
            String unit,
            String source,
            boolean measured,
            boolean estimated,
            String cadence,
            String hardwareSource,
            String validitySemantics) {
        this.key = Objects.requireNonNull(key, "key");
        this.type = Objects.requireNonNull(type, "type");
        this.unit = unit == null ? "" : unit;
        this.source = source == null ? "" : source;
        this.measured = measured;
        this.estimated = estimated;
        this.cadence = cadence == null ? "" : cadence;
        this.hardwareSource = hardwareSource == null ? "" : hardwareSource;
        this.validitySemantics = validitySemantics == null ? "" : validitySemantics;
    }

    public String key() {
        return key;
    }

    public LogValueType type() {
        return type;
    }

    public String unit() {
        return unit;
    }

    public String source() {
        return source;
    }

    public boolean measured() {
        return measured;
    }

    public boolean estimated() {
        return estimated;
    }

    public String cadence() {
        return cadence;
    }

    public String hardwareSource() {
        return hardwareSource;
    }

    public String validitySemantics() {
        return validitySemantics;
    }

    public String metadataJson() {
        StringBuilder sb = new StringBuilder(192);
        sb.append('{');
        Json.appendString(sb, "unit", unit);
        sb.append(',');
        Json.appendString(sb, "source", source);
        sb.append(',');
        Json.appendBoolean(sb, "measured", measured);
        sb.append(',');
        Json.appendBoolean(sb, "estimated", estimated);
        sb.append(',');
        Json.appendString(sb, "cadence", cadence);
        sb.append(',');
        Json.appendString(sb, "schemaVersion", org.allsparks.amper.AmperVersion.LOG_SCHEMA_VERSION);
        sb.append(',');
        Json.appendString(sb, "hardwareSource", hardwareSource);
        sb.append(',');
        Json.appendString(sb, "validity", validitySemantics);
        sb.append('}');
        return sb.toString();
    }
}
