package org.allsparks.amper.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.measure.PowerMonitor;
import org.junit.jupiter.api.Test;

class CsvFormatTest {
    @Test
    void escapesCommasAndQuotes() {
        assertEquals("\"a,b\"", CsvFormat.escape("a,b"));
        assertEquals("\"a\"\"b\"", CsvFormat.escape("a\"b"));
        assertEquals("12.500000", CsvFormat.number(12.5));
        assertEquals("NaN", CsvFormat.number(Double.NaN));
        assertEquals("amper_match.csv", CsvFormat.sanitizeFilename("amper match"));
        assertEquals("amper.._etc_passwd.csv", CsvFormat.sanitizeFilename("../etc/passwd"));
    }

    @Test
    void schemaHeaderIsStable() {
        PowerMonitor monitor = new PowerMonitor(
                () -> 42L,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.emptyList(),
                1.0,
                100_000_000L,
                5.0,
                16.0);
        PowerEventLogger logger = new PowerEventLogger(16);
        logger.recordObservation(monitor.update());
        String csv = logger.exportCsv();
        assertTrue(csv.startsWith("# amper_csv_schema=1"));
        assertTrue(csv.contains("timestampNanos,type,message,fields"));
        assertTrue(csv.contains("rawV=12.0000"));
        assertTrue(csv.contains("sourceId=hub"));
    }
}
