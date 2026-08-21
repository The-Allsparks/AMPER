package org.allsparks.amper.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.log.PowerEventLogger;
import org.junit.jupiter.api.Test;

class PowerMonitorTest {
    @Test
    void updateDoesNotChangeCommandedEffort() {
        AtomicLong time = new AtomicLong(1_000_000L);
        AmperClock clock = time::get;

        RevHubTelemetrySource hub = RevHubTelemetrySource.voltageOnly("Control Hub", () -> 12.4);
        RevMotorTelemetry motor = new RevMotorTelemetry("fl", () -> 2.5, () -> 0.75, () -> 100.0, () -> 10.0, true);

        PowerMonitor monitor =
                new PowerMonitor(clock, hub, Collections.singletonList(motor), 0.5, 100_000_000L, 5.0, 16.0);

        assertEquals(0.75, motor.commandedEffort(), 1e-9);
        ElectricalObservation obs = monitor.update();
        assertTrue(obs.sensingValid());
        assertEquals(12.4, obs.rawVoltage().volts(), 1e-9);
        assertEquals(0.75, motor.commandedEffort(), 1e-9);
        assertEquals(1, obs.motors().size());
        assertEquals("fl", obs.motors().get(0).motorId());
        assertEquals(0.75, obs.motors().get(0).commandedEffort(), 1e-9);
        assertEquals(2.5, obs.motorCurrents().get(0).amps(), 1e-9);
    }

    @Test
    void missingVoltageDegradesCleanly() {
        AmperClock clock = () -> 5_000_000L;
        PowerTelemetrySource source = new PowerTelemetrySource() {
            @Override
            public VoltageSample readBusVoltage(long nowNanos) {
                return VoltageSample.missing(nowNanos, "missing");
            }

            @Override
            public CurrentSample readBatteryCurrent(long nowNanos) {
                return CurrentSample.unsupported(nowNanos, "missing");
            }

            @Override
            public String sourceName() {
                return "missing";
            }
        };

        PowerMonitor monitor = new PowerMonitor(
                clock, source, Collections.<MotorElectricalTelemetry>emptyList(), 0.5, 100_000_000L, 5.0, 16.0);
        ElectricalObservation obs = monitor.update();
        assertFalse(obs.sensingValid());
        assertEquals(MeasurementValidity.MISSING, obs.rawVoltage().validity());
    }

    @Test
    void loggerExportsObservation() {
        AmperClock clock = () -> 42L;
        RevHubTelemetrySource hub = RevHubTelemetrySource.voltageOnly("hub", () -> 12.0);
        PowerMonitor monitor = new PowerMonitor(
                clock, hub, Collections.<MotorElectricalTelemetry>emptyList(), 1.0, 100_000_000L, 5.0, 16.0);
        PowerEventLogger logger = new PowerEventLogger(16);
        logger.recordObservation(monitor.update());
        String csv = logger.exportCsv();
        assertTrue(csv.contains("LOOP_SAMPLE"));
        assertTrue(csv.contains("rawV=12.0000"));
        assertTrue(csv.contains("sumAbsCmd=0.0000"));
    }

    @Test
    void unsupportedMotorCurrentIsAllowed() {
        AmperClock clock = () -> 7L;
        RevHubTelemetrySource hub = RevHubTelemetrySource.voltageOnly("hub", () -> 12.1);
        RevMotorTelemetry motor = new RevMotorTelemetry("intake", null, () -> 1.0, null, null, false);
        PowerMonitor monitor = new PowerMonitor(clock, hub, Arrays.asList(motor), 1.0, 100_000_000L, 5.0, 16.0);
        ElectricalObservation obs = monitor.update();
        assertEquals(MeasurementValidity.UNSUPPORTED, obs.motorCurrents().get(0).validity());
        assertTrue(obs.sensingValid());
    }
}
