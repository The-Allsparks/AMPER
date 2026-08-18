package org.allsparks.amper.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.adapters.rev.RevMotorTelemetry;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.PowerMonitor;
import org.allsparks.amper.policy.PowerPolicy;
import org.junit.jupiter.api.Test;

class MechanismAndStallTest {

    @Test
    void startRequiresHysteresisThreshold() {
        PowerPolicy policy = PowerPolicy.defaults();
        MechanismActivityTracker tracker = new MechanismActivityTracker();
        PowerEventLogger logger = new PowerEventLogger(32);

        ElectricalObservation idle = observation(0.02, 0.0, 100.0, 12.4);
        tracker.update(idle, policy, logger);
        assertEquals(0, tracker.startCount());

        ElectricalObservation spinning = observation(0.5, 1.0, 200.0, 12.4);
        tracker.update(spinning, policy, logger);
        assertEquals(1, tracker.startCount());
    }

    @Test
    void stallRequiresDwell() {
        PowerPolicy policy = PowerPolicy.builder().stallDwellNanos(100L).build();
        StallSuspicionTracker tracker = new StallSuspicionTracker();
        PowerEventLogger logger = new PowerEventLogger(32);

        ElectricalObservation first = observationAt(0L, 1.0, 10.0, 5.0, 12.0);
        assertFalse(tracker.update(first, policy, logger));
        ElectricalObservation second = observationAt(150L, 1.0, 10.0, 4.0, 12.0);
        assertTrue(tracker.update(second, policy, logger));
        assertTrue(logger.exportCsv().contains("STALL_SUSPECTED"));
    }

    @Test
    void stallDwellSurvivesSkippedRoundRobinCurrent() {
        PowerPolicy policy = PowerPolicy.builder().stallDwellNanos(100L).build();
        StallSuspicionTracker tracker = new StallSuspicionTracker();
        java.util.concurrent.atomic.AtomicLong time = new java.util.concurrent.atomic.AtomicLong(0L);
        RevMotorTelemetry stalled = new RevMotorTelemetry(
                "intake", () -> 12.0, () -> 1.0, () -> 1.0, () -> 0.0, true);
        RevMotorTelemetry free = new RevMotorTelemetry(
                "drive", () -> 1.0, () -> 0.2, () -> 200.0, () -> 0.0, true);
        PowerMonitor monitor = new PowerMonitor(
                time::get,
                java.util.Collections.singletonList(RevHubTelemetrySource.voltageOnly("hub", () -> 12.0)),
                0,
                java.util.Arrays.asList(stalled, free),
                1.0,
                100_000_000L,
                5.0,
                16.0,
                org.allsparks.amper.policy.SamplingPolicy.recommended(),
                0.10);

        ElectricalObservation first = monitor.update();
        assertEquals(MeasurementValidity.VALID, first.motors().get(0).current().validity());
        assertFalse(tracker.update(first, policy, null));

        time.set(20L);
        ElectricalObservation skipped = monitor.update();
        assertEquals(MeasurementValidity.SKIPPED, skipped.motors().get(0).current().validity());
        assertEquals(12.0, skipped.motors().get(0).current().amps(), 1e-9);
        assertFalse(tracker.update(skipped, policy, null));

        time.set(150L);
        ElectricalObservation later = monitor.update();
        assertTrue(tracker.update(later, policy, null));
        assertTrue(tracker.suspectedMotorIds().contains("intake"));
    }

    @Test
    void missingCurrentDoesNotInventStall() {
        PowerPolicy policy = PowerPolicy.builder().stallDwellNanos(100L).build();
        StallSuspicionTracker tracker = new StallSuspicionTracker();
        ElectricalObservation missing = observationAt(0L, 1.0, Double.NaN, 1.0, 12.0);
        assertFalse(tracker.update(missing, policy, null));
        ElectricalObservation later = observationAt(200L, 1.0, Double.NaN, 1.0, 12.0);
        assertFalse(tracker.update(later, policy, null));
        assertTrue(tracker.suspectedMotorIds().isEmpty());
    }

    private static ElectricalObservation observation(
            double effort, double amps, double velocity, double volts) {
        return observationAt(1L, effort, amps, velocity, volts);
    }

    private static ElectricalObservation observationAt(
            long time, double effort, double amps, double velocity, double volts) {
        AmperClock clock = () -> time;
        RevMotorTelemetry motor = new RevMotorTelemetry(
                "intake", () -> amps, () -> effort, () -> velocity, () -> 0.0, true);
        PowerMonitor monitor = new PowerMonitor(
                clock,
                RevHubTelemetrySource.voltageOnly("hub", () -> volts),
                Collections.singletonList(motor),
                1.0,
                100_000_000L,
                5.0,
                16.0);
        return monitor.update();
    }
}
