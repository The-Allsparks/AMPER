package org.allsparks.amper.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.allsparks.amper.AmperFeatureFlags;
import org.allsparks.amper.api.PowerGrant;
import org.allsparks.amper.api.PowerLimitReason;
import org.allsparks.amper.api.PowerPriority;
import org.allsparks.amper.api.PowerRequest;
import org.allsparks.amper.coord.PowerCoordinator;
import org.junit.jupiter.api.Test;

class PowerPolicyAndCoordinatorTest {
    @Test
    void defaultsDisableIntervention() {
        AmperFeatureFlags flags = AmperFeatureFlags.defaults();
        assertTrue(flags.isPhase0Measurement());
        assertFalse(flags.isPhase1PassiveTelemetry());
        assertFalse(flags.isAnyInterventionEnabled());
        AmperFeatureFlags passive = AmperFeatureFlags.passiveTelemetry();
        assertTrue(passive.isPhase1PassiveTelemetry());
        assertFalse(passive.isAnyInterventionEnabled());
    }

    @Test
    void coordinatorPassThroughWhenInterventionDisabled() {
        PowerPolicy policy = PowerPolicy.defaults();
        PowerCoordinator coordinator = new PowerCoordinator(policy);
        PowerRequest request = new PowerRequest(
                "drive",
                0.8,
                0.2,
                PowerPriority.DRIVETRAIN_NORMAL,
                false,
                false,
                true,
                0L,
                Double.NaN);
        PowerGrant grant = coordinator.allocate(Collections.singletonList(request)).get(0);
        assertTrue(grant.allowedEffort() == 0.8);
        assertTrue(grant.reason() == PowerLimitReason.FEATURE_DISABLED);
    }
}
