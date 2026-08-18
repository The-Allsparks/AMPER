package org.allsparks.amper.protect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.amper.api.PowerLimitReason;
import org.junit.jupiter.api.Test;

class LocalProtectionTest {
    @Test
    void disabledPathIsIdentity() {
        LocalProtection protection = LocalProtection.disabled();
        ConstrainedCommand result = protection.apply(0.73, 1_000_000L);
        assertEquals(0.73, result.requested(), 0.0);
        assertEquals(0.73, result.allowed(), 0.0);
        assertFalse(result.constrained());
        assertEquals(PowerLimitReason.FEATURE_DISABLED, result.reason());
    }

    @Test
    void slewLimitsRate() {
        SlewRateLimiter slew = new SlewRateLimiter(1.0);
        assertEquals(0.0, slew.apply(0.0, 0L), 1e-9);
        double limited = slew.apply(1.0, 500_000_000L);
        assertEquals(0.5, limited, 1e-9);
        double next = slew.apply(1.0, 1_000_000_000L);
        assertEquals(1.0, next, 1e-9);
    }

    @Test
    void zeroDtKeepsLastOutput() {
        SlewRateLimiter slew = new SlewRateLimiter(2.0);
        slew.apply(0.1, 10L);
        assertEquals(0.1, slew.apply(1.0, 10L), 1e-9);
    }

    @Test
    void negativeTimeRejected() {
        SlewRateLimiter slew = new SlewRateLimiter(1.0);
        slew.apply(0.0, 10L);
        assertThrows(IllegalArgumentException.class, () -> slew.apply(0.0, 9L));
    }

    @Test
    void stallNoiseDoesNotTripWithoutDwell() {
        org.allsparks.amper.telemetry.StallSuspicionTracker tracker =
                new org.allsparks.amper.telemetry.StallSuspicionTracker();
        org.allsparks.amper.policy.PowerPolicy policy =
                org.allsparks.amper.policy.PowerPolicy.builder().stallDwellNanos(100L).build();
        org.allsparks.amper.sim.SimulatedClock clock = new org.allsparks.amper.sim.SimulatedClock();
        org.allsparks.amper.sim.SimulatedHubSource hub = new org.allsparks.amper.sim.SimulatedHubSource("hub");
        org.allsparks.amper.sim.SimulatedMotor motor = new org.allsparks.amper.sim.SimulatedMotor("intake");
        motor.setCommand(1.0);
        motor.setCurrentAmps(12.0);
        motor.setVelocity(1.0);
        hub.setVolts(12.0);
        org.allsparks.amper.measure.PowerMonitor monitor = org.allsparks.amper.measure.PowerMonitor.create(
                clock, hub, java.util.Collections.singletonList(motor), policy);
        assertFalse(tracker.update(monitor.update(), policy, null));
        clock.set(50L);
        assertFalse(tracker.update(monitor.update(), policy, null));
        clock.set(200L);
        assertTrue(tracker.update(monitor.update(), policy, null));
    }

    @Test
    void gravityCriticalRequiresDeclaration() {
        assertThrows(IllegalArgumentException.class, () -> GravityHoldPolicy.declare("", 0.2));
        assertThrows(IllegalArgumentException.class, () -> GravityHoldPolicy.declare("lift", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> LocalProtection.builder()
                .enabled(true)
                .gravityCritical(null)
                .build());
        GravityHoldPolicy hold = GravityHoldPolicy.declare("lift", 0.15);
        assertEquals(0.15, hold.enforce(0.0, 1.0), 1e-9);
        assertEquals(-0.15, hold.enforce(0.0, -1.0), 1e-9);
    }
}
