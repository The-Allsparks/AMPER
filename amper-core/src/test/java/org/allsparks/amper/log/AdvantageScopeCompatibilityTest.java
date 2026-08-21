package org.allsparks.amper.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdvantageScopeCompatibilityTest {
    @Test
    void tableHeaderSpellingAndUnits() {
        String csv = CanonicalLogExamples.tableCsv();
        String header = csv.substring(0, csv.indexOf('\n'));
        assertTrue(header.startsWith("Timestamp,"), header);
        assertTrue(header.contains("/AMPER/System/BusVoltageVolts"));
        assertTrue(header.contains("/AMPER/System/FilteredVoltageVolts"));
        assertTrue(header.contains("/AMPER/Motors/frontLeft/CurrentAmps"));
        assertTrue(header.contains("/AMPER/Motors/frontLeft/Command"));
        assertFalse(header.contains("TotalCurrentAmps"));
        assertTrue(header.contains("/AMPER/System/SelectedMotorsCurrentAmps"));
        assertFalse(csv.contains("NaN"));
    }

    @Test
    void timestampsAreSecondsMonotonicAndLocaleIndependent() {
        AdvantageScopeCsv.ParsedTable table = AdvantageScopeCsv.parseTable(CanonicalLogExamples.tableCsv());
        assertEquals(4, table.rows.size());
        double previous = Double.NEGATIVE_INFINITY;
        for (AdvantageScopeCsv.ParsedRow row : table.rows) {
            assertTrue(row.timestampSeconds.indexOf(',') < 0, row.timestampSeconds);
            assertTrue(row.timestampSeconds.indexOf('.') >= 0, "seconds must be decimal: " + row.timestampSeconds);
            double seconds = row.timestampAsSeconds();
            assertTrue(seconds >= previous, "timestamps must be nondecreasing");
            previous = seconds;
        }
        assertEquals("0.000000000", table.rows.get(0).timestampSeconds);
        assertEquals("0.020000000", table.rows.get(1).timestampSeconds);
    }

    @Test
    void missingNumericsAreEmptyAndBooleansAreLiterals() {
        AdvantageScopeCsv.ParsedTable table = AdvantageScopeCsv.parseTable(CanonicalLogExamples.tableCsv());
        AdvantageScopeCsv.ParsedRow first = table.rows.get(0);
        assertEquals("", first.cells.get(LogKeys.motorCurrentAmps("frontLeft")));
        assertEquals("false", first.cells.get(LogKeys.motorStallSuspected("frontLeft")));
        AdvantageScopeCsv.ParsedRow second = table.rows.get(1);
        assertEquals("1.820000", second.cells.get(LogKeys.motorCurrentAmps("frontLeft")));
        AdvantageScopeCsv.ParsedRow missingVoltage = table.rows.get(2);
        assertEquals("", missingVoltage.cells.get(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS));
        assertEquals("sag, warning", missingVoltage.cells.get(LogKeys.EVENTS_MESSAGE));
        assertTrue(CanonicalLogExamples.tableCsv().contains("\"sag, warning\""));
        assertEquals("true", table.rows.get(3).cells.get(LogKeys.motorStallSuspected("frontLeft")));
    }

    @Test
    void stringsAreQuotedAndListLayoutHeaderIsExact() {
        String list = AdvantageScopeCsv.list(CanonicalLogExamples.representativeSession());
        String header = list.substring(0, list.indexOf('\n'));
        assertEquals("Timestamp,Key,Value", header);
        assertTrue(list.contains("\"mechanism_start\"") || list.contains("mechanism_start"));
        CanonicalLog log = CanonicalLogExamples.representativeSession();
        LogValue message = log.samples().get(2).get(LogKeys.EVENTS_MESSAGE);
        assertEquals("sag, warning", message.asString());
        String table = CanonicalLogExamples.tableCsv();
        assertTrue(table.contains("\"sag, warning\""));
    }

    @Test
    void committedFixtureMatchesGenerator() throws Exception {
        Path dir = fixturesDir();
        String generated = CanonicalLogExamples.tableCsv();
        String committed =
                new String(Files.readAllBytes(dir.resolve("amper-advantagescope-table.csv")), StandardCharsets.UTF_8);
        assertEquals(normalize(generated), normalize(committed));
        String schema = new String(
                Files.readAllBytes(dir.resolve("amper-advantagescope-table.schema.json")), StandardCharsets.UTF_8);
        assertTrue(schema.contains("\"timestampUnit\": \"seconds\""));
        assertTrue(schema.contains("/AMPER/System/BusVoltageVolts"));
        assertTrue(schema.contains("hardwareNameMapping"));
        assertTrue(schema.contains("Control Hub"));
    }

    @Test
    void listLayoutIsValidCsv() {
        AdvantageScopeCsv.ParsedTable ignored = AdvantageScopeCsv.parseTable(CanonicalLogExamples.tableCsv());
        assertFalse(ignored.keys.isEmpty());
        String list = AdvantageScopeCsv.list(CanonicalLogExamples.representativeSession());
        List<String> lines = AdvantageScopeCsv.splitLines(list);
        for (int i = 1; i < lines.size(); i++) {
            List<String> cells = AdvantageScopeCsv.parseRow(lines.get(i));
            assertEquals(3, cells.size(), lines.get(i));
        }
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }

    static Path fixturesDir() {
        Path module = Paths.get("").toAbsolutePath();
        Path docs = module.resolve("docs/logging/fixtures");
        if (Files.isDirectory(docs)) {
            return docs;
        }
        Path fromModule = module.resolve("../docs/logging/fixtures");
        if (Files.isDirectory(fromModule)) {
            return fromModule.normalize();
        }
        return module.resolve("../../docs/logging/fixtures").normalize();
    }
}
