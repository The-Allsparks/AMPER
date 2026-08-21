package org.allsparks.amper.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.api.DriverPowerState;
import org.allsparks.amper.battery.BatteryObservation;
import org.allsparks.amper.battery.EstimateConfidence;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.PowerMonitor;
import org.allsparks.amper.policy.PowerPolicy;
import org.junit.jupiter.api.Test;

class DriverFeedbackTest {

    @Test
    void hysteresisHoldsElevatedUntilRecovery() {
        PowerPolicy policy = PowerPolicy.defaults();
        ElectricalObservation watch = voltage(10.8);
        DriverPowerState classified = DriverFeedback.classify(watch, battery(12.4, 12.1), false, policy);
        assertEquals(DriverPowerState.ELEVATED_DEMAND, classified);

        ElectricalObservation stillLow = voltage(11.05);
        DriverPowerState held = DriverFeedback.applyHysteresis(
                DriverPowerState.ELEVATED_DEMAND, DriverPowerState.NORMAL, stillLow, policy);
        assertEquals(DriverPowerState.ELEVATED_DEMAND, held);

        ElectricalObservation recovered = voltage(11.4);
        DriverPowerState released = DriverFeedback.applyHysteresis(
                DriverPowerState.ELEVATED_DEMAND, DriverPowerState.NORMAL, recovered, policy);
        assertEquals(DriverPowerState.NORMAL, released);
    }

    @Test
    void invalidSensingWins() {
        ElectricalObservation missing = new PowerMonitor(
                        () -> 1L,
                        RevHubTelemetrySource.voltageOnly("hub", () -> Double.NaN),
                        Collections.emptyList(),
                        1.0,
                        100_000_000L,
                        5.0,
                        16.0)
                .update();
        DriverPowerState state = DriverFeedback.classify(missing, null, true, PowerPolicy.defaults());
        assertEquals(DriverPowerState.INVALID_SENSING, state);
    }

    @Test
    void weakBatteryClearsWhenRecentWindowRecovers() {
        PowerPolicy policy = PowerPolicy.defaults();
        ElectricalObservation loaded = voltage(10.8);
        DriverPowerState weak = DriverFeedback.classify(loaded, battery(12.6, 10.8), false, policy);
        assertEquals(DriverPowerState.SUSPECTED_WEAK_BATTERY, weak);

        ElectricalObservation recovered = voltage(12.4);
        DriverPowerState normal = DriverFeedback.classify(recovered, battery(12.4, 12.4), false, policy);
        assertEquals(DriverPowerState.NORMAL, normal);
    }

    private static ElectricalObservation voltage(double volts) {
        return new PowerMonitor(
                        () -> 1L,
                        RevHubTelemetrySource.voltageOnly("hub", () -> volts),
                        Collections.emptyList(),
                        1.0,
                        100_000_000L,
                        5.0,
                        16.0)
                .update();
    }

    private static BatteryObservation battery(double rest, double loaded) {
        return new BatteryObservation(loaded, rest, loaded, new EstimateConfidence(0.5, "test"));
    }
}
