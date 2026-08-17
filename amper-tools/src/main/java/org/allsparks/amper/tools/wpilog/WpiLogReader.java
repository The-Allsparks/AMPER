package org.allsparks.amper.tools.wpilog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Independent WPILOG reader for round-trip verification. Implements WPILib
 * Data Log File Format 1.0 (allwpilib v2026.2.1 {@code datalog.adoc}).
 */
public final class WpiLogReader {
    private WpiLogReader() {
    }

    public static ParsedLog read(byte[] data) {
        if (data == null || data.length < 12) {
            throw new IllegalArgumentException("WPILOG too short");
        }
        if (data[0] != 'W' || data[1] != 'P' || data[2] != 'I'
                || data[3] != 'L' || data[4] != 'O' || data[5] != 'G') {
            throw new IllegalArgumentException("missing WPILOG magic");
        }
        int version = (data[6] & 0xFF) | ((data[7] & 0xFF) << 8);
        if (version != 0x0100) {
            throw new IllegalArgumentException("unsupported WPILOG version: " + version);
        }
        long extraLenLong = readUnsigned32(data, 8);
        if (extraLenLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("extra header too large");
        }
        int extraLen = (int) extraLenLong;
        int offset = 12;
        if (offset + extraLen > data.length) {
            throw new IllegalArgumentException("truncated extra header");
        }
        ParsedLog parsed = new ParsedLog();
        parsed.version = version;
        parsed.extraHeader = new String(data, offset, extraLen, StandardCharsets.UTF_8);
        offset += extraLen;

        while (offset < data.length) {
            int bitfield = data[offset] & 0xFF;
            int idLen = (bitfield & 0x3) + 1;
            int sizeLen = ((bitfield >> 2) & 0x3) + 1;
            int tsLen = ((bitfield >> 4) & 0x7) + 1;
            offset++;
            if (offset + idLen + sizeLen + tsLen > data.length) {
                throw new IllegalArgumentException("truncated record header");
            }
            long entryId = readUnsigned(data, offset, idLen);
            offset += idLen;
            long payloadSize = readUnsigned(data, offset, sizeLen);
            offset += sizeLen;
            long timestampUs = readUnsigned(data, offset, tsLen);
            offset += tsLen;
            if (payloadSize > Integer.MAX_VALUE || offset + (int) payloadSize > data.length) {
                throw new IllegalArgumentException("truncated record payload");
            }
            byte[] payload = new byte[(int) payloadSize];
            System.arraycopy(data, offset, payload, 0, payload.length);
            offset += payload.length;
            if (entryId == 0L) {
                parseControl(parsed, timestampUs, payload);
            } else {
                Entry entry = parsed.entries.get(Integer.valueOf((int) entryId));
                if (entry != null) {
                    entry.samples.add(new Sample(timestampUs, decode(entry.type, payload)));
                }
                parsed.dataRecords++;
            }
        }
        return parsed;
    }

    private static void parseControl(ParsedLog parsed, long timestampUs, byte[] payload) {
        if (payload.length < 1) {
            return;
        }
        int type = payload[0] & 0xFF;
        if (type == 0) {
            if (payload.length < 13) {
                throw new IllegalArgumentException("truncated Start control record");
            }
            int cursor = 1;
            int entryId = (int) readUnsigned(payload, cursor, 4);
            cursor += 4;
            int nameLen = (int) readUnsigned(payload, cursor, 4);
            cursor += 4;
            String name = new String(payload, cursor, nameLen, StandardCharsets.UTF_8);
            cursor += nameLen;
            int typeLen = (int) readUnsigned(payload, cursor, 4);
            cursor += 4;
            String typeName = new String(payload, cursor, typeLen, StandardCharsets.UTF_8);
            cursor += typeLen;
            int metaLen = (int) readUnsigned(payload, cursor, 4);
            cursor += 4;
            String metadata = new String(payload, cursor, metaLen, StandardCharsets.UTF_8);
            Entry entry = new Entry();
            entry.id = entryId;
            entry.name = name;
            entry.type = typeName;
            entry.metadata = metadata;
            entry.startTimestampUs = timestampUs;
            parsed.entries.put(Integer.valueOf(entryId), entry);
            parsed.entriesByName.put(name, entry);
        }
    }

    static Object decode(String type, byte[] payload) {
        if ("boolean".equals(type)) {
            return Boolean.valueOf(payload.length > 0 && payload[0] != 0);
        }
        if ("double".equals(type)) {
            return Double.valueOf(Double.longBitsToDouble(readSigned64(payload, 0)));
        }
        if ("int64".equals(type)) {
            return Long.valueOf(readSigned64(payload, 0));
        }
        if ("float".equals(type)) {
            int bits = (payload[0] & 0xFF)
                    | ((payload[1] & 0xFF) << 8)
                    | ((payload[2] & 0xFF) << 16)
                    | ((payload[3] & 0xFF) << 24);
            return Float.valueOf(Float.intBitsToFloat(bits));
        }
        return new String(payload, StandardCharsets.UTF_8);
    }

    static long readUnsigned32(byte[] data, int offset) {
        return readUnsigned(data, offset, 4);
    }

    static long readUnsigned(byte[] data, int offset, int bytes) {
        long value = 0L;
        for (int i = 0; i < bytes; i++) {
            value |= ((long) (data[offset + i] & 0xFF)) << (8 * i);
        }
        return value;
    }

    static long readSigned64(byte[] data, int offset) {
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (data[offset + i] & 0xFF)) << (8 * i);
        }
        return value;
    }

    public static final class ParsedLog {
        public int version;
        public String extraHeader = "";
        public final Map<Integer, Entry> entries = new LinkedHashMap<Integer, Entry>();
        public final Map<String, Entry> entriesByName = new LinkedHashMap<String, Entry>();
        public int dataRecords;
    }

    public static final class Entry {
        public int id;
        public String name;
        public String type;
        public String metadata;
        public long startTimestampUs;
        public final List<Sample> samples = new ArrayList<Sample>();

        public List<Sample> samplesView() {
            return Collections.unmodifiableList(samples);
        }
    }

    public static final class Sample {
        public final long timestampUs;
        public final Object value;

        public Sample(long timestampUs, Object value) {
            this.timestampUs = timestampUs;
            this.value = value;
        }
    }
}
