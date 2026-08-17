package org.allsparks.amper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.log.FileSessionLogSink;
import org.allsparks.amper.log.SessionMetadata;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AmperLifecycleAndSafetyTest {
    @Test
    void disabledProducesNoHardwareReads() {
        AtomicLong reads = new AtomicLong();
        AtomicLong time = new AtomicLong(1_000_000L);
        AmperSession session = new AmperSession(
                AmperPolicies.disabled(),
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", () -> {
                    reads.incrementAndGet();
                    return 12.0;
                }),
                Collections.emptyList());
        ElectricalObservation obs = session.observe();
        assertTrue(obs.disabled());
        assertEquals(0L, reads.get());
        assertEquals("AMPER_DISABLED", session.driverTelemetry().message());
    }

    @Test
    void duplicateObserveDoesNotResample() {
        AtomicLong reads = new AtomicLong();
        AtomicLong time = new AtomicLong(5_000_000L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.builder().duplicateObserveWindowNanos(2_000_000L).build())
                .build();
        AmperSession session = new AmperSession(
                policy,
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", () -> {
                    reads.incrementAndGet();
                    return 12.2;
                }),
                Collections.emptyList());
        session.observe();
        long firstReads = reads.get();
        session.observe();
        assertEquals(firstReads, reads.get());
        assertEquals(1, session.duplicateObserveCount());
        assertTrue(session.exportCsv().contains("DUPLICATE_OBSERVE"));
    }

    @Test
    void missingVoltageIsNotValidAndNotZero() {
        AmperSession session = new AmperSession(
                AmperPolicies.passiveDefaults(),
                () -> 1L,
                RevHubTelemetrySource.voltageOnly("hub", () -> Double.NaN),
                Collections.emptyList());
        ElectricalObservation obs = session.observe();
        assertFalse(obs.sensingValid());
        assertEquals(MeasurementValidity.MISSING, obs.rawVoltage().validity());
        assertTrue(Double.isNaN(obs.rawVoltage().volts()));
    }

    @Test
    void closeRejectsFurtherObserve() {
        AmperSession session = new AmperSession(
                AmperPolicies.measurementOnly(),
                () -> 1L,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.emptyList());
        session.observe();
        session.close();
        assertThrows(IllegalStateException.class, session::observe);
    }

    @Test
    void stopFlushesCsv(@TempDir Path dir) throws Exception {
        FileSessionLogSink sink = new FileSessionLogSink(dir.toFile());
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.everyLoop())
                .build();
        AmperSession session = new AmperSession(
                policy,
                () -> 42L,
                java.util.Collections.singletonList(RevHubTelemetrySource.voltageOnly("hub", () -> 12.4)),
                0,
                Collections.emptyList(),
                SessionMetadata.anonymous("test"),
                sink,
                "amper test.csv");
        session.observe();
        session.stop();
        Path written = dir.resolve("amper_test.csv");
        assertTrue(Files.isRegularFile(written));
        String csv = new String(Files.readAllBytes(written), StandardCharsets.UTF_8);
        assertTrue(csv.contains("# amper_csv_schema=1"));
        assertTrue(csv.contains("MATCH_SUMMARY"));
    }

    @Test
    void loggerCapacityIsBounded() {
        PowerPolicy policy = PowerPolicy.builder().loggerCapacity(3).build();
        org.allsparks.amper.sim.SimulatedClock sim = new org.allsparks.amper.sim.SimulatedClock();
        AmperSession bounded = new AmperSession(
                policy,
                sim,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.emptyList());
        for (int i = 0; i < 10; i++) {
            sim.set(i * 20_000_000L);
            bounded.observe();
        }
        assertTrue(bounded.logger().snapshot().size() <= 3);
        assertTrue(bounded.logger().droppedCount() > 0);
    }
}
