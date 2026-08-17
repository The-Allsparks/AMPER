package org.allsparks.amper.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.amper.AmperFeatureFlags;
import org.junit.jupiter.api.Test;

class PowerPolicyValidationTest {
    @Test
    void rejectsUnorderedVoltageThresholds() {
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .watchVoltageVolts(9.0)
                .limitingVoltageVolts(10.0)
                .criticalVoltageVolts(11.0)
                .recoveryVoltageVolts(12.0)
                .build());
    }

    @Test
    void rejectsNonFiniteAndNegativeDurations() {
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .voltageFilterAlpha(Double.NaN)
                .build());
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .recoveryHoldNanos(-1L)
                .build());
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .staleAfterNanos(0L)
                .build());
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .loggerCapacity(0)
                .build());
        assertThrows(IllegalArgumentException.class, () -> PowerPolicy.builder()
                .stallCurrentAmps(-1.0)
                .build());
    }

    @Test
    void placeholdersAreNotHardwareValidated() {
        PowerPolicy policy = PowerPolicy.defaults();
        assertEquals(ThresholdProvenance.CONSERVATIVE_PLACEHOLDER, policy.voltageThresholdProvenance());
        assertFalse(policy.featureFlags().isAnyInterventionEnabled());
        assertTrue(policy.featureFlags().isPhase0Measurement());
        assertFalse(policy.featureFlags().isPhase1PassiveTelemetry());
    }

    @Test
    void interventionFlagsDefaultFalse() {
        AmperFeatureFlags flags = AmperFeatureFlags.defaults();
        assertFalse(flags.isPhase2LocalProtection());
        assertFalse(flags.isPhase3ReactiveVoltage());
        assertFalse(flags.isPhase4Coordination());
        assertFalse(flags.isPhase5PredictiveEstimate());
        assertTrue(flags.isPhase5ShadowOnly());
        assertFalse(flags.isPhase6PredictiveShaping());
        assertFalse(flags.isPhase7Adaptive());
        assertFalse(flags.isAnyInterventionEnabled());
    }
}
