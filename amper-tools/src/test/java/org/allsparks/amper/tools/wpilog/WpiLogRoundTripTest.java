package org.allsparks.amper.tools.wpilog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.allsparks.amper.log.CanonicalLog;
import org.allsparks.amper.log.CanonicalLogExamples;
import org.allsparks.amper.log.LogKeys;
import org.allsparks.amper.tools.AmperConvert;
import org.junit.jupiter.api.Test;

class WpiLogRoundTripTest {
    @Test
    void roundTripPreservesKeysTypesTimestampsAndCounts() throws Exception {
        CanonicalLog log = CanonicalLogExamples.representativeSession();
        byte[] bytes = WpiLogWriter.toBytes(log);
        assertEquals('W', bytes[0]);
        assertEquals('P', bytes[1]);
        assertEquals('I', bytes[2]);
        assertEquals('L', bytes[3]);
        assertEquals('O', bytes[4]);
        assertEquals('G', bytes[5]);
        assertEquals(0x00, bytes[6]);
        assertEquals(0x01, bytes[7]);

        WpiLogReader.ParsedLog parsed = WpiLogReader.read(bytes);
        assertEquals(0x0100, parsed.version);
        assertTrue(parsed.extraHeader.contains("AMPER"));
        assertTrue(parsed.entriesByName.containsKey(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS));
        assertEquals("double", parsed.entriesByName.get(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS).type);
        assertEquals("boolean", parsed.entriesByName.get(LogKeys.motorStallSuspected("frontLeft")).type);
        assertEquals("string", parsed.entriesByName.get(LogKeys.EVENTS_MESSAGE).type);
        assertEquals("int64", parsed.entriesByName.get(LogKeys.PERFORMANCE_DROPPED_RECORDS).type);

        WpiLogReader.Entry voltage = parsed.entriesByName.get(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS);
        assertEquals(3, voltage.samples.size());
        assertEquals(0L, voltage.samples.get(0).timestampUs);
        assertEquals(20_000L, voltage.samples.get(1).timestampUs);
        assertEquals(60_000L, voltage.samples.get(2).timestampUs);
        assertEquals(12.73, ((Double) voltage.samples.get(0).value).doubleValue(), 1e-9);

        WpiLogReader.Entry current = parsed.entriesByName.get(LogKeys.motorCurrentAmps("frontLeft"));
        assertEquals(2, current.samples.size());
        assertEquals(1.82, ((Double) current.samples.get(0).value).doubleValue(), 1e-9);

        WpiLogReader.Entry message = parsed.entriesByName.get(LogKeys.EVENTS_MESSAGE);
        boolean sawWarning = false;
        for (WpiLogReader.Sample sample : message.samples) {
            if ("sag, warning".equals(sample.value)) {
                sawWarning = true;
            }
        }
        assertTrue(sawWarning);

        Path out = Paths.get("build/wpilog-fixture/amper-fixture.wpilog");
        Files.createDirectories(out.getParent());
        Files.write(out, bytes);
        assertTrue(Files.size(out) > 0);
    }

    @Test
    void csvConversionSurvivesReader() throws Exception {
        String csv = CanonicalLogExamples.tableCsv();
        CanonicalLog rebuilt = AmperConvert.fromTableCsv(csv);
        byte[] bytes = WpiLogWriter.toBytes(rebuilt);
        WpiLogReader.ParsedLog parsed = WpiLogReader.read(bytes);
        assertTrue(parsed.entriesByName.containsKey(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS));
        assertEquals(4, rebuilt.size());
        assertEquals(CanonicalLogExamples.representativeSession().schema().size(), parsed.entriesByName.size());
        Files.createDirectories(Paths.get("build/wpilog-fixture"));
        Files.write(Paths.get("build/wpilog-fixture/amper-from-csv.wpilog"), bytes);
        assertTrue(new String(csv.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).startsWith("Timestamp,"));
    }
}
