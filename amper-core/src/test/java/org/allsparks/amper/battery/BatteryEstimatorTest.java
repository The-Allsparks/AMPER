package org.allsparks.amper.battery;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.measure.PowerMonitor;
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
}
