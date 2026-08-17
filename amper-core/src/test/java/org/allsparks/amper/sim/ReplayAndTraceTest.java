package org.allsparks.amper.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.log.PowerEvent;
import org.junit.jupiter.api.Test;

class ReplayAndTraceTest {
    @Test
    void tracesAreDeterministicAndNotHardwareValidation() {
        List<ElectricalObservation> a = TraceGenerator.healthyBattery();
        List<ElectricalObservation> b = TraceGenerator.healthyBattery();
        assertEquals(a.size(), b.size());
        assertEquals(a.get(10).filteredVoltage().volts(), b.get(10).filteredVoltage().volts(), 0.0);
        List<ElectricalObservation> weak = TraceGenerator.weakHighResistanceBattery();
        assertTrue(weak.get(weak.size() - 1).rawVoltage().volts()
                < TraceGenerator.healthyBattery().get(TraceGenerator.healthyBattery().size() - 1).rawVoltage().volts());
        assertTrue(TraceGenerator.voltageNoise().size() > 0);
        assertTrue(TraceGenerator.simultaneousMechanismStarts().size() > 0);
        assertTrue(TraceGenerator.loopTimingSpikes().size() > 0);
        assertTrue(TraceGenerator.stalledIntake().size() > 0);
    }

    @Test
    void missingAndStaleAreExplicit() {
        List<ElectricalObservation> missing = TraceGenerator.missingSamples();
        boolean sawMissing = false;
        for (ElectricalObservation obs : missing) {
            if (obs.rawVoltage().validity() == MeasurementValidity.MISSING) {
                sawMissing = true;
            }
        }
        assertTrue(sawMissing);
        ElectricalObservation stale = TraceGenerator.staleSamples().get(0);
        assertEquals(MeasurementValidity.STALE, stale.rawVoltage().validity());
        assertFalse(stale.sensingValid());
    }

    @Test
    void csvReplayRoundTrip() {
        List<ElectricalObservation> healthy = TraceGenerator.healthyBattery();
        org.allsparks.amper.AmperSession session = sessionFromHealthy();
        String csv = session.exportCsv();
        List<PowerEvent> events = CsvReplay.parse(csv);
        assertFalse(events.isEmpty());
        assertTrue(csv.contains("rawV="));
        List<PowerEvent> loops = CsvReplay.loopSamples(csv);
        assertFalse(loops.isEmpty());
        assertTrue(healthy.size() > 0);
    }

    private static org.allsparks.amper.AmperSession sessionFromHealthy() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        hub.setVolts(12.5);
        org.allsparks.amper.policy.PowerPolicy policy = org.allsparks.amper.policy.PowerPolicy.builder()
                .featureFlags(org.allsparks.amper.AmperFeatureFlags.passiveTelemetry())
                .build();
        org.allsparks.amper.AmperSession session = new org.allsparks.amper.AmperSession(
                policy, clock, hub, java.util.Collections.<org.allsparks.amper.measure.MotorElectricalTelemetry>emptyList());
        session.observe();
        return session;
    }
}
