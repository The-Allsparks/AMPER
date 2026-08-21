package org.allsparks.amper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.allsparks.amper.api.DriverPowerState;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.log.LogKeys;
import org.allsparks.amper.log.LogValue;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;
import org.junit.jupiter.api.Test;

class AmperSessionTest {

    @Test
    void observeDoesNotChangeCommandedEffort() {
        AtomicReference<Double> effort = new AtomicReference<>(0.6);
        AtomicReference<Double> volts = new AtomicReference<>(12.4);
        AtomicLong time = new AtomicLong(1_000_000L);

        AmperSession session = session(effort, volts, time, 0.0, true);
        assertEquals(0.6, effort.get(), 1e-9);
        session.observe();
        assertEquals(0.6, effort.get(), 1e-9);
        session.observe();
        assertEquals(0.6, effort.get(), 1e-9);
        assertFalse(session.policy().featureFlags().isAnyInterventionEnabled());
    }

    @Test
    void phase1LogsCommandAndStartStop() {
        AtomicReference<Double> effort = new AtomicReference<>(0.0);
        AtomicReference<Double> volts = new AtomicReference<>(12.5);
        AtomicLong time = new AtomicLong(0L);

        AmperSession session = session(effort, volts, time, 0.0, true);
        session.start();
        session.observe();
        effort.set(0.8);
        time.addAndGet(20_000_000L);
        session.observe();
        effort.set(0.0);
        time.addAndGet(20_000_000L);
        session.observe();

        String csv = session.exportCsv();
        assertTrue(csv.contains("m0Cmd="));
        assertTrue(csv.contains("mechanism_start"));
        assertTrue(csv.contains("mechanism_stop"));
        assertEquals(1, session.matchSummary().mechanismStarts());
        assertEquals(1, session.matchSummary().mechanismStops());
    }

    @Test
    void warningsDoNotAppearWhenPhase1Disabled() {
        AtomicReference<Double> volts = new AtomicReference<>(9.0);
        AtomicLong time = new AtomicLong(0L);
        PowerPolicy policy = PowerPolicy.defaults();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", volts::get), Collections.emptyList());
        session.observe();
        assertEquals("PHASE1_DISABLED", session.driverTelemetry().message());
        boolean hasVoltageWarning = false;
        for (PowerEvent event : session.logger().snapshot()) {
            if (event.type() == PowerEventType.VOLTAGE_WARNING) {
                hasVoltageWarning = true;
            }
        }
        assertFalse(hasVoltageWarning);
    }

    @Test
    void severeVoltageWarningWhenPhase1Enabled() {
        AtomicReference<Double> volts = new AtomicReference<>(9.0);
        AtomicLong time = new AtomicLong(1_000_000L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .build();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", volts::get), Collections.emptyList());
        session.start();
        ElectricalObservation obs = session.observe();
        assertTrue(obs.sensingValid());
        assertEquals(
                DriverPowerState.SEVERE_VOLTAGE_RISK, session.driverTelemetry().state());
        assertTrue(session.exportCsv().contains("SEVERE_VOLTAGE_RISK"));
    }

    @Test
    void canonicalRowAnnotatesSameTimestampWarning() {
        AtomicReference<Double> volts = new AtomicReference<>(9.0);
        AtomicLong time = new AtomicLong(1_000_000L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .build();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", volts::get), Collections.emptyList());
        session.start();
        session.observe();
        assertEquals(
                "VOLTAGE_WARNING", session.logger().lastAnnotatingEvent().type().name());
        LogValue annotated = session.canonicalLog()
                .samples()
                .get(session.canonicalLog().size() - 1)
                .get(LogKeys.EVENTS_TYPE);
        assertTrue(annotated != null && annotated.present());
        assertEquals("VOLTAGE_WARNING", annotated.asString());
    }

    @Test
    void driverPublishIsRateLimited() {
        AtomicReference<Double> volts = new AtomicReference<>(12.6);
        AtomicLong time = new AtomicLong(0L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .telemetryMinPeriodNanos(100_000_000L)
                .build();
        AmperSession session = new AmperSession(
                policy, time::get, RevHubTelemetrySource.voltageOnly("hub", volts::get), Collections.emptyList());
        session.start();
        assertTrue(session.observe() != null);
        assertTrue(session.driverTelemetry().publishedThisCycle());
        time.addAndGet(10_000_000L);
        session.observe();
        assertFalse(session.driverTelemetry().publishedThisCycle());
        time.addAndGet(100_000_000L);
        session.observe();
        assertTrue(session.driverTelemetry().publishedThisCycle());
    }

    @Test
    void matchSummaryRecordsLoopOverhead() {
        AtomicLong time = new AtomicLong(0L);
        AmperClock advancing = () -> time.addAndGet(1_000L);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .build();
        AmperSession session = new AmperSession(
                policy, advancing, RevHubTelemetrySource.voltageOnly("hub", () -> 12.2), Collections.emptyList());
        session.start();
        session.observe();
        session.recordMatchSummary();
        assertEquals(1L, session.matchSummary().sampleCount());
        assertTrue(session.matchSummary().maxLoopNanos() > 0L);
        assertTrue(session.matchSummary().p95LoopNanos() > 0L);
        assertTrue(
                session.matchSummary().p95LoopNanos() <= session.matchSummary().maxLoopNanos());
        assertTrue(session.exportCsv().contains("MATCH_SUMMARY"));
        assertTrue(session.exportCsv().contains("p95LoopNs="));
    }

    private static AmperSession session(
            AtomicReference<Double> effort,
            AtomicReference<Double> volts,
            AtomicLong time,
            double currentAmps,
            boolean phase1) {
        RevMotorTelemetry motor =
                new RevMotorTelemetry("intake", () -> currentAmps, effort::get, () -> 200.0, () -> 0.0, true);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(phase1 ? AmperFeatureFlags.passiveTelemetry() : AmperFeatureFlags.defaults())
                .build();
        return new AmperSession(
                policy,
                time::get,
                RevHubTelemetrySource.voltageOnly("hub", volts::get),
                Collections.singletonList(motor));
    }
}
