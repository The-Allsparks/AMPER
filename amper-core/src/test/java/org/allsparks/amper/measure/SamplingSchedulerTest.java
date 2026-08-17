package org.allsparks.amper.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.allsparks.amper.policy.SamplingPolicy;
import org.junit.jupiter.api.Test;

class SamplingSchedulerTest {
    @Test
    void roundRobinReadsOneCurrentPerLoop() {
        AtomicLong time = new AtomicLong(1_000_000L);
        AtomicInteger aReads = new AtomicInteger();
        AtomicInteger bReads = new AtomicInteger();
        RevMotorTelemetry a = new RevMotorTelemetry(
                "a",
                () -> {
                    aReads.incrementAndGet();
                    return 1.0;
                },
                () -> 0.2,
                () -> 10.0,
                () -> 0.0,
                true);
        RevMotorTelemetry b = new RevMotorTelemetry(
                "b",
                () -> {
                    bReads.incrementAndGet();
                    return 2.0;
                },
                () -> 0.3,
                () -> 11.0,
                () -> 0.0,
                true);
        PowerMonitor monitor = new PowerMonitor(
                time::get,
                java.util.Collections.singletonList(RevHubTelemetrySource.voltageOnly("hub", () -> 12.0)),
                0,
                Arrays.asList(a, b),
                1.0,
                100_000_000L,
                5.0,
                16.0,
                SamplingPolicy.recommended(),
                0.10);

        ElectricalObservation first = monitor.update();
        assertEquals(1, first.samplingStats().currentReadsThisLoop());
        assertTrue(first.motors().get(0).currentReadThisLoop());
        assertFalse(first.motors().get(1).currentReadThisLoop());
        assertEquals(MeasurementValidity.SKIPPED, first.motors().get(1).current().validity());
        assertEquals(1, aReads.get());
        assertEquals(0, bReads.get());

        time.addAndGet(20_000_000L);
        ElectricalObservation second = monitor.update();
        assertTrue(second.motors().get(1).currentReadThisLoop());
        assertFalse(second.motors().get(0).currentReadThisLoop());
        assertEquals(MeasurementValidity.SKIPPED, second.motors().get(0).current().validity());
        assertEquals(1.0, second.motors().get(0).current().amps(), 1e-9);
        assertEquals(1, aReads.get());
        assertEquals(1, bReads.get());
    }

    @Test
    void skippedCurrentIsNotLabeledValid() {
        AtomicLong time = new AtomicLong(0L);
        RevMotorTelemetry motor = new RevMotorTelemetry(
                "m", () -> 3.0, () -> 0.5, () -> 1.0, () -> 0.0, true);
        PowerMonitor monitor = new PowerMonitor(
                time::get,
                java.util.Collections.singletonList(RevHubTelemetrySource.voltageOnly("hub", () -> 12.0)),
                0,
                java.util.Collections.singletonList(motor),
                1.0,
                50_000_000L,
                5.0,
                16.0,
                SamplingPolicy.builder().maxCurrentReadsPerLoop(0).build(),
                0.10);
        ElectricalObservation obs = monitor.update();
        assertFalse(obs.motors().get(0).current().isUsable());
        assertEquals(MeasurementValidity.SKIPPED, obs.motors().get(0).current().validity());
    }
}
