package org.allsparks.amper.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.policy.SamplingPolicy;
import org.junit.jupiter.api.Test;

class MultiHubMonitorTest {
    @Test
    void policySourceIsExplicitAndAllHubsAreLabeled() {
        RevHubTelemetrySource control = RevHubTelemetrySource.voltageOnly("Control Hub", () -> 12.4);
        RevHubTelemetrySource expansion = RevHubTelemetrySource.voltageOnly("Expansion Hub 1", () -> 12.1);
        PowerMonitor monitor = new PowerMonitor(
                () -> 5L,
                Arrays.asList(control, expansion),
                0,
                java.util.Collections.<MotorElectricalTelemetry>emptyList(),
                1.0,
                100_000_000L,
                5.0,
                16.0,
                SamplingPolicy.everyLoop(),
                0.10);
        ElectricalObservation obs = monitor.update();
        assertEquals(2, obs.allVoltages().size());
        assertEquals("Control Hub", obs.rawVoltage().sourceId());
        assertEquals(12.4, obs.rawVoltage().volts(), 1e-9);
        assertEquals("Expansion Hub 1", obs.allVoltages().get(1).sourceId());
        assertEquals(12.1, obs.allVoltages().get(1).volts(), 1e-9);
        assertTrue(obs.sensingValid());
    }
}
