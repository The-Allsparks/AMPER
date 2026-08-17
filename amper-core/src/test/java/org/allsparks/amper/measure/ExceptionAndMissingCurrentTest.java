package org.allsparks.amper.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.junit.jupiter.api.Test;

class ExceptionAndMissingCurrentTest {
    @Test
    void currentExceptionBecomesFailedMeasurementNotZero() {
        RevMotorTelemetry motor = new RevMotorTelemetry(
                "intake",
                () -> {
                    throw new RuntimeException("i2c");
                },
                () -> 0.4,
                () -> 10.0,
                () -> 0.0,
                true);
        PowerMonitor monitor = new PowerMonitor(
                () -> 3L,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.singletonList(motor),
                1.0,
                100_000_000L,
                5.0,
                16.0);
        ElectricalObservation obs = monitor.update();
        assertEquals(MeasurementValidity.MISSING, obs.motors().get(0).current().validity());
        assertTrue(Double.isNaN(obs.motors().get(0).current().amps()));
        assertTrue(obs.samplingStats().failedThisLoop() >= 1L);
    }
}
