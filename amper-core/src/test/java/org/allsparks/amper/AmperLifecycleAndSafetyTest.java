package org.allsparks.amper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.log.FileSessionLogSink;
import org.allsparks.amper.log.SessionLogSink;
import org.allsparks.amper.log.SessionMetadata;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;
import org.allsparks.amper.telemetry.TelemetrySink;
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
                .sampling(SamplingPolicy.builder()
                        .duplicateObserveWindowNanos(2_000_000L)
                        .build())
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
        session.start();
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
        session.start();
        session.observe();
        session.stop();
        Path written = dir.resolve("amper_test.csv");
        assertTrue(Files.isRegularFile(written));
        String csv = new String(Files.readAllBytes(written), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("Timestamp,"), csv);
        assertTrue(csv.contains("/AMPER/System/BusVoltageVolts"));
        Path sidecar = dir.resolve("amper_test.schema.json");
        assertTrue(Files.isRegularFile(sidecar));
        String schema = new String(Files.readAllBytes(sidecar), StandardCharsets.UTF_8);
        assertTrue(schema.contains("\"timestampUnit\": \"seconds\""));
    }

    @Test
    void sinkFailureDoesNotChangeObservation() {
        SessionLogSink failing = new SessionLogSink() {
            @Override
            public void export(String filename, String csvContents) throws java.io.IOException {
                throw new java.io.IOException("disk full");
            }
        };
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
                failing,
                "amper-session.csv");
        session.start();
        ElectricalObservation observation = session.observe();
        assertEquals(12.4, observation.rawVoltage().volts(), 1e-9);
        session.stop();
        assertTrue(session.sinkFailureCount() >= 1);
        assertEquals(12.4, session.monitor().lastObservation().rawVoltage().volts(), 1e-9);
        assertEquals(12.4, observation.rawVoltage().volts(), 1e-9);
    }

    @Test
    void loggerCapacityIsBounded() {
        PowerPolicy policy = PowerPolicy.builder().loggerCapacity(3).build();
        org.allsparks.amper.sim.SimulatedClock sim = new org.allsparks.amper.sim.SimulatedClock();
        AmperSession bounded = new AmperSession(
                policy, sim, RevHubTelemetrySource.voltageOnly("hub", () -> 12.0), Collections.emptyList());
        for (int i = 0; i < 10; i++) {
            sim.set(i * 20_000_000L);
            if (i == 0) {
                bounded.start();
            }
            bounded.observe();
        }
        assertTrue(bounded.logger().snapshot().size() <= 3);
        assertTrue(bounded.logger().droppedCount() > 0);
    }

    @Test
    void measurementOnlyPublishTelemetryShowsVoltage() {
        AtomicLong time = new AtomicLong(0L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.defaults())
                .telemetryMinPeriodNanos(50_000_000L)
                .build();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", () -> 12.3), Collections.emptyList());
        RecordingSink sink = new RecordingSink();
        session.start();
        session.observe();
        session.publishTelemetry(sink);
        assertEquals("PHASE1_DISABLED", sink.last.get("AMPER"));
        assertEquals(12.3, sink.last.get("AMPER.V"));
        assertTrue(sink.last.containsKey("AMPER.loopUs"));
        assertTrue(sink.last.containsKey("AMPER.p95Us"));
        assertTrue(sink.last.containsKey("AMPER.maxUs"));
        assertEquals(1, sink.updates);

        session.publishTelemetry(sink);
        assertEquals(1, sink.updates);

        time.addAndGet(60_000_000L);
        session.observe();
        session.publishTelemetry(sink);
        assertEquals(2, sink.updates);
        assertEquals(12.3, sink.last.get("AMPER.V"));
    }

    @Test
    void initObserveStaysInitializedAndSkipsMatchAccounting() {
        AtomicLong time = new AtomicLong(1_000_000L);
        AmperSession session = new AmperSession(
                AmperPolicies.passiveDefaults(),
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", () -> 11.0),
                Collections.emptyList());
        session.initialize();
        session.observe();
        assertEquals(AmperLifecycle.INITIALIZED, session.lifecycle());
        assertEquals(0L, session.matchSummary().sampleCount());
        assertFalse(session.exportCsv().contains("LOOP_SAMPLE"));
    }

    @Test
    void startClearsInitSamplesFromMatchSummary() {
        AtomicLong time = new AtomicLong(0L);
        AtomicReference<Double> volts = new AtomicReference<>(11.0);
        AmperSession session = new AmperSession(
                AmperPolicies.passiveDefaults(),
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", volts::get),
                Collections.emptyList());
        session.initialize();
        session.observe();
        volts.set(12.5);
        time.addAndGet(20_000_000L);
        session.start();
        session.observe();
        assertEquals(AmperLifecycle.STARTED, session.lifecycle());
        assertEquals(1L, session.matchSummary().sampleCount());
        assertTrue(session.exportAdvantageScopeTableCsv().contains("/AMPER/System/BusVoltageVolts"));
    }

    @Test
    void omittedStartLeavesMatchSummaryEmpty() {
        AmperSession session = new AmperSession(
                AmperPolicies.passiveDefaults(),
                () -> 1L,
                RevHubTelemetrySource.voltageOnly("hub", () -> 11.8),
                Collections.emptyList());
        session.initialize();
        session.observe();
        session.stop();
        assertEquals(0L, session.matchSummary().sampleCount());
        assertTrue(Double.isNaN(session.matchSummary().minVoltage()));
    }

    @Test
    void stoppedSessionRequiresInitializeBeforeObserve() {
        AmperSession session = new AmperSession(
                AmperPolicies.measurementOnly(),
                () -> 1L,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.emptyList());
        session.start();
        session.observe();
        session.stop();
        assertThrows(IllegalStateException.class, session::observe);
        session.initialize();
        session.observe();
        assertEquals(AmperLifecycle.INITIALIZED, session.lifecycle());
    }

    @Test
    void disabledPublishTelemetryShowsStateWithoutVoltage() {
        AtomicLong time = new AtomicLong(0L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(
                        AmperFeatureFlags.builder().phase0Measurement(false).build())
                .telemetryMinPeriodNanos(50_000_000L)
                .build();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", () -> 12.0), Collections.emptyList());
        RecordingSink sink = new RecordingSink();
        session.start();
        session.observe();
        session.publishTelemetry(sink);
        assertEquals("AMPER_DISABLED", sink.last.get("AMPER"));
        assertFalse(sink.last.containsKey("AMPER.V"));
        assertEquals(1, sink.updates);

        session.publishTelemetry(sink);
        assertEquals(1, sink.updates);

        time.addAndGet(60_000_000L);
        session.observe();
        session.publishTelemetry(sink);
        assertEquals(2, sink.updates);
        assertEquals("AMPER_DISABLED", sink.last.get("AMPER"));
        assertFalse(sink.last.containsKey("AMPER.V"));
    }

    @Test
    void sessionLocalProtectionHonorsKillSwitchAndClock() {
        AtomicLong time = new AtomicLong(0L);
        AmperSession closedGate = new AmperSession(
                AmperPolicies.passiveDefaults(),
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.0),
                Collections.emptyList());
        org.allsparks.amper.protect.ConstrainedCommand identity =
                closedGate.constrain(closedGate.localProtection(true), 0.8);
        assertEquals(0.8, identity.allowed(), 0.0);
        assertFalse(identity.constrained());

        PowerPolicy openPolicy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.builder()
                        .phase1PassiveTelemetry(true)
                        .phase2LocalProtection(true)
                        .build())
                .slewMaxDeltaPerSecond(1.0)
                .build();
        AmperSession openGate = new AmperSession(
                openPolicy, time::get, RevHubTelemetrySource.voltageOnly("hub", () -> 12.0), Collections.emptyList());
        org.allsparks.amper.protect.LocalProtection protection = openGate.localProtection(true);
        assertEquals(0.0, openGate.constrain(protection, 0.0).allowed(), 1e-9);
        time.set(500_000_000L);
        org.allsparks.amper.protect.ConstrainedCommand limited = openGate.constrain(protection, 1.0);
        assertEquals(0.5, limited.allowed(), 1e-9);
        assertTrue(limited.constrained());
    }

    private static final class RecordingSink implements TelemetrySink {
        private final Map<String, Object> last = new LinkedHashMap<String, Object>();
        private int updates;

        @Override
        public void addData(String key, Object value) {
            last.put(key, value);
        }

        @Override
        public void update() {
            updates++;
        }
    }
}
