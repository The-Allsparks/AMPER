package org.allsparks.amper.protect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.sim.SimulatedClock;
import org.allsparks.amper.sim.SimulatedHubSource;
import org.junit.jupiter.api.Test;

class VoltageStateMachineTest {
    @Test
    void sensorFaultDisablesIntervention() {
        VoltageStateMachine fsm = new VoltageStateMachine();
        PowerPolicy policy = PowerPolicy.defaults();
        SimulatedClock clock = new SimulatedClock(1L);
        SimulatedHubSource hub = new SimulatedHubSource("hub");
        hub.setMissing();
        ElectricalObservation obs = org.allsparks.amper.measure.PowerMonitor.create(
                        clock, hub, java.util.Collections.emptyList(), policy)
                .update();
        assertEquals(VoltageProtectionState.SENSOR_FAULT, fsm.update(obs, policy));
        assertFalse(fsm.interventionPermitted(policy));
    }

    @Test
    void recoveryRequiresDwell() {
        VoltageStateMachine fsm = new VoltageStateMachine();
        PowerPolicy policy = PowerPolicy.builder().recoveryHoldNanos(100L).build();
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("hub");
        hub.setVolts(9.0);
        ElectricalObservation low = org.allsparks.amper.measure.PowerMonitor.create(
                        clock, hub, java.util.Collections.emptyList(), policy)
                .update();
        assertEquals(VoltageProtectionState.CRITICAL, fsm.update(low, policy));
        hub.setVolts(12.0);
        clock.set(50L);
        ElectricalObservation early = org.allsparks.amper.measure.PowerMonitor.create(
                        clock, hub, java.util.Collections.emptyList(), policy)
                .update();
        assertEquals(VoltageProtectionState.CRITICAL, fsm.update(early, policy));
    }
}
