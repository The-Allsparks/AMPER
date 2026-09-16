package org.allsparks.amper.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StudentSamplingPolicyTest {
    @Test
    void studentPresetsDoNotPollMotorCurrent() {
        assertEquals(0, AmperPolicies.measurementOnly().sampling().maxCurrentReadsPerLoop());
        assertEquals(0, AmperPolicies.passiveDefaults().sampling().maxCurrentReadsPerLoop());
        assertEquals(0, AmperPolicies.disabled().sampling().maxCurrentReadsPerLoop());
        assertEquals(0, AmperPolicies.localProtectionAllowed().sampling().maxCurrentReadsPerLoop());
        assertEquals(0, SamplingPolicy.hubCurrentPreferred().maxCurrentReadsPerLoop());
    }

    @Test
    void characterizationRoundRobinStillReadsOneMotorPerLoop() {
        assertEquals(1, SamplingPolicy.recommended().maxCurrentReadsPerLoop());
    }
}
