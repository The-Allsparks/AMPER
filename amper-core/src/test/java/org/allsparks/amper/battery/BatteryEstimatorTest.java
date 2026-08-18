package org.allsparks.amper.battery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.measure.PowerMonitor;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.sim.SimulatedClock;
import org.junit.jupiter.api.Test;

class BatteryEstimatorTest {
    @Test
    void neverClaimsCertainty() {
        AmperClock clock = () -> 1L;
        PowerMonitor monitor = new PowerMonitor(
                clock,
                RevHubTelemetrySource.voltageOnly("hub", () -> 12.6),
                Collections.emptyList(),
                1.0,
                100_000_000L,
                5.0,
                16.0);
        BatteryEstimator estimator = new BatteryEstimator();
        BatteryObservation observation = estimator.update(monitor.update());
        assertTrue(observation.confidence().score() < 1.0);
        assertTrue(observation.confidence().note().toLowerCase().contains("not a predictive"));
    }

    @Test
    void oneSagDoesNotLatchHintsAfterWindow() {
        SimulatedClock clock = new SimulatedClock();
        AtomicReference<Double> volts = new AtomicReference<Double>(12.6);
        PowerMonitor monitor = new PowerMonitor(
                clock,
                RevHubTelemetrySource.voltageOnly("hub", volts::get),
                Collections.emptyList(),
                1.0,
                100_000_000L,
                5.0,
                16.0);
        BatteryEstimator estimator = new BatteryEstimator();
        PowerPolicy policy = PowerPolicy.defaults();

        BatteryObservation rest = estimator.update(monitor.update());
        assertEquals(12.6, rest.restingHintVolts(), 1e-9);

        clock.set(20_000_000L);
        volts.set(10.8);
        BatteryObservation sag = estimator.update(monitor.update());
        assertTrue(sag.restingHintVolts() - sag.loadedHintVolts() >= policy.weakBatterySagVolts());
        assertTrue(sag.loadedHintVolts() <= policy.watchVoltageVolts());

        clock.set(BatteryEstimator.HINT_WINDOW_NANOS + 40_000_000L);
        volts.set(12.4);
        BatteryObservation recovered = estimator.update(monitor.update());
        assertTrue(recovered.restingHintVolts() - recovered.loadedHintVolts() < policy.weakBatterySagVolts());
    }
}
