package org.allsparks.amper.tools.wpilog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.allsparks.amper.AmperVersion;
import org.allsparks.amper.log.CanonicalLog;
import org.allsparks.amper.log.CanonicalSample;
import org.allsparks.amper.log.Json;
import org.allsparks.amper.log.LogFieldSpec;
import org.allsparks.amper.log.LogValue;
import org.allsparks.amper.log.LogValueType;

/**
 * Pure-Java WPILOG writer from the WPILib Data Log File Format 1.0
 * ({@code datalog.adoc}, allwpilib v2026.2.1). Desktop only — not invoked by
 * the FTC runtime.
 */
public final class WpiLogWriter {
    private static final byte[] MAGIC = new byte[] {'W', 'P', 'I', 'L', 'O', 'G'};
    private static final int VERSION = 0x0100;

    private WpiLogWriter() {}

    public static byte[] toBytes(CanonicalLog log) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, log);
        return out.toByteArray();
    }

    public static void write(OutputStream out, CanonicalLog log) throws IOException {
        byte[] extra = extraHeader().getBytes(StandardCharsets.UTF_8);
        out.write(MAGIC);
        writeFixedLe(out, VERSION, 2);
        writeFixedLe(out, extra.length, 4);
        out.write(extra);

        int nextId = 1;
        java.util.LinkedHashMap<String, Integer> ids = new java.util.LinkedHashMap<String, Integer>();
        for (Map.Entry<String, LogFieldSpec> entry : log.schema().entrySet()) {
            int id = nextId++;
            ids.put(entry.getKey(), Integer.valueOf(id));
            writeStart(out, id, entry.getValue(), 0L);
        }

        long origin = log.originNanos();
        for (CanonicalSample sample : log.samples()) {
            long timestampUs = Math.max(0L, (sample.timestampNanos() - origin) / 1000L);
            for (Map.Entry<String, LogValue> valueEntry : sample.values().entrySet()) {
                LogValue value = valueEntry.getValue();
                if (value == null || !value.present()) {
                    continue;
                }
                Integer id = ids.get(valueEntry.getKey());
                if (id == null) {
                    continue;
                }
                writeData(out, id.intValue(), timestampUs, value);
            }
        }
        out.flush();
    }

    private static String extraHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Json.appendString(sb, "source", "AMPER");
        sb.append(',');
        Json.appendString(sb, "schemaVersion", AmperVersion.LOG_SCHEMA_VERSION);
        sb.append(',');
        Json.appendString(sb, "libraryVersion", AmperVersion.VERSION);
        sb.append(',');
        Json.appendString(sb, "converter", "amper-tools");
        sb.append('}');
        return sb.toString();
    }

    private static void writeStart(OutputStream out, int entryId, LogFieldSpec spec, long timestampUs)
            throws IOException {
        byte[] name = spec.key().getBytes(StandardCharsets.UTF_8);
        byte[] type = wpiType(spec.type()).getBytes(StandardCharsets.UTF_8);
        byte[] metadata = spec.metadataJson().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(0);
        writeFixedLe(payload, entryId, 4);
        writeFixedLe(payload, name.length, 4);
        payload.write(name);
        writeFixedLe(payload, type.length, 4);
        payload.write(type);
        writeFixedLe(payload, metadata.length, 4);
        payload.write(metadata);
        writeRecord(out, 0, timestampUs, payload.toByteArray());
    }

    private static void writeData(OutputStream out, int entryId, long timestampUs, LogValue value) throws IOException {
        byte[] payload;
        switch (value.type()) {
            case BOOLEAN:
                payload = new byte[] {value.asBoolean() ? (byte) 1 : (byte) 0};
                break;
            case DOUBLE:
                payload = new byte[8];
                putLongLe(payload, 0, Double.doubleToLongBits(value.asDouble()));
                break;
            case INT64:
                payload = new byte[8];
                putLongLe(payload, 0, value.asInt64());
                break;
            case STRING:
            default:
                payload = (value.asString() == null ? "" : value.asString()).getBytes(StandardCharsets.UTF_8);
                break;
        }
        writeRecord(out, entryId, timestampUs, payload);
    }

    static void writeRecord(OutputStream out, int entryId, long timestampUs, byte[] payload) throws IOException {
        int idLen = unsignedLength(entryId & 0xFFFFFFFFL, 4);
        int sizeLen = unsignedLength(payload.length & 0xFFFFFFFFL, 4);
        int tsLen = unsignedLength(timestampUs, 8);
        int bitfield = (idLen - 1) | ((sizeLen - 1) << 2) | ((tsLen - 1) << 4);
        out.write(bitfield);
        writeFixedLe(out, entryId, idLen);
        writeFixedLe(out, payload.length, sizeLen);
        writeFixedLe(out, timestampUs, tsLen);
        out.write(payload);
    }

    static int unsignedLength(long value, int maxBytes) {
        long u = value;
        int n = 1;
        long max = 0xFFL;
        while (n < maxBytes && u > max) {
            n++;
            max = (max << 8) | 0xFFL;
        }
        return n;
    }

    static void writeFixedLe(OutputStream out, long value, int bytes) throws IOException {
        for (int i = 0; i < bytes; i++) {
            out.write((int) ((value >>> (8 * i)) & 0xFF));
        }
    }

    static void putLongLe(byte[] dest, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            dest[offset + i] = (byte) ((value >>> (8 * i)) & 0xFF);
        }
    }

    static String wpiType(LogValueType type) {
        switch (type) {
            case BOOLEAN:
                return "boolean";
            case DOUBLE:
                return "double";
            case INT64:
                return "int64";
            case STRING:
            default:
                return "string";
        }
    }
}
