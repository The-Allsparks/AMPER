package org.allsparks.amper.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CanonicalLogTypeStabilityTest {
    @Test
    void rejectsTypeChangesAndKeepsPriorType() {
        CanonicalLog log = new CanonicalLog(8);
        log.register(FieldSpecs.busVoltage());
        CanonicalSample first = CanonicalSample.at(0L)
                .putDouble(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS, 12.5)
                .build();
        log.append(first);
        CanonicalSample mismatch = CanonicalSample.at(1_000_000L)
                .putString(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS, "twelve")
                .build();
        log.append(mismatch);
        assertEquals(1L, log.typeMismatchCount());
        assertEquals(LogValueType.DOUBLE, log.schema().get(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS).type());
        assertTrue(log.samples().get(1).get(LogKeys.SYSTEM_BUS_VOLTAGE_VOLTS) == null);
    }

    @Test
    void sanitizesHierarchyBreakingNames() {
        LogNameSanitizer names = new LogNameSanitizer();
        assertEquals("Control_Hub", names.sanitize("Control Hub"));
        assertEquals("intake_left", names.sanitize("intake/left"));
        assertEquals("frontLeft", names.sanitize("frontLeft"));
        assertEquals("unnamed", names.sanitize("///"));
        assertEquals("Control_Hub", names.sanitize("Control Hub"));
        assertEquals("Control Hub", names.mapping().get("Control Hub") == null
                ? names.mapping().keySet().iterator().next()
                : "Control Hub");
        assertEquals("Control_Hub", names.mapping().get("Control Hub"));
    }

    @Test
    void secondsFormattingIsLocaleIndependent() {
        assertEquals("0.000000000", CsvFormat.secondsFromNanos(0L));
        assertEquals("0.020000000", CsvFormat.secondsFromNanos(20_000_000L));
        assertEquals("true", CsvFormat.booleanLiteral(true));
        assertEquals("false", CsvFormat.booleanLiteral(false));
        assertEquals("\"a,b\"", CsvFormat.quoteString("a,b"));
        assertEquals("\"a\"\"b\"", CsvFormat.quoteString("a\"b"));
        assertEquals("amper_test.schema.json", CsvFormat.sidecarFilename("amper test.csv"));
        double[] samples = {0.0, 0.2, 12.4, 12.5, -0.5, 1.23456, 9.0, 0.00004, 0.00006};
        for (int i = 0; i < samples.length; i++) {
            assertEquals(
                    String.format(java.util.Locale.US, "%.4f", samples[i]),
                    CsvFormat.fixed4(samples[i]));
        }
        assertEquals("NaN", CsvFormat.fixed4(Double.NaN));
    }
}
