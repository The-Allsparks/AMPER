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
