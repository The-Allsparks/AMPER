package org.allsparks.amper.log;

import java.util.Objects;

/**
 * Typed optional payload for one field at one timestamp.
 *
 * <p>Missing numeric measurements are represented as {@link #missing(LogValueType)},
 * never as zero or NaN.
 */
public final class LogValue {
    private final LogValueType type;
    private final boolean present;
    private final double doubleValue;
    private final boolean booleanValue;
    private final String stringValue;
    private final long int64Value;

    private LogValue(
            LogValueType type,
            boolean present,
            double doubleValue,
            boolean booleanValue,
            String stringValue,
            long int64Value) {
        this.type = Objects.requireNonNull(type, "type");
        this.present = present;
        this.doubleValue = doubleValue;
        this.booleanValue = booleanValue;
        this.stringValue = stringValue;
        this.int64Value = int64Value;
    }

    public static LogValue missing(LogValueType type) {
        return new LogValue(type, false, Double.NaN, false, null, 0L);
    }

    public static LogValue ofDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return missing(LogValueType.DOUBLE);
        }
        return new LogValue(LogValueType.DOUBLE, true, value, false, null, 0L);
    }

    public static LogValue ofBoolean(boolean value) {
        return new LogValue(LogValueType.BOOLEAN, true, Double.NaN, value, null, 0L);
    }

    public static LogValue ofString(String value) {
        if (value == null) {
            return missing(LogValueType.STRING);
        }
        return new LogValue(LogValueType.STRING, true, Double.NaN, false, value, 0L);
    }

    public static LogValue ofInt64(long value) {
        return new LogValue(LogValueType.INT64, true, Double.NaN, false, null, value);
    }

    public LogValueType type() {
        return type;
    }

    public boolean present() {
        return present;
    }

    public double asDouble() {
        return doubleValue;
    }

    public boolean asBoolean() {
        return booleanValue;
    }

    public String asString() {
        return stringValue;
    }

    public long asInt64() {
        return int64Value;
    }
}
