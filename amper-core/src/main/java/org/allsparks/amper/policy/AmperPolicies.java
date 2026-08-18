package org.allsparks.amper.policy;

import org.allsparks.amper.AmperFeatureFlags;

/**
 * Named policy presets. Voltage numbers remain conservative placeholders
 * until a team records Control Hub characterization.
 */
public final class AmperPolicies {
    private AmperPolicies() {
    }

    /** Phase 0 only: measure, log, no Phase 1 warnings, no intervention. */
    public static PowerPolicy measurementOnly() {
        return PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.defaults())
                .sampling(SamplingPolicy.recommended())
                .voltageThresholdProvenance(ThresholdProvenance.CONSERVATIVE_PLACEHOLDER)
                .build();
    }

    /**
     * Phase 0 + Phase 1 warnings. Motor outputs are still never modified.
     * Current sampling uses recommended round-robin cadence.
     */
    public static PowerPolicy passiveDefaults() {
        return PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.recommended())
                .voltageThresholdProvenance(ThresholdProvenance.CONSERVATIVE_PLACEHOLDER)
                .build();
    }

    /** Fully disabled: {@link AmperFeatureFlags#isPhase0Measurement()} is false. */
    public static PowerPolicy disabled() {
        return PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.builder().phase0Measurement(false).build())
                .sampling(SamplingPolicy.recommended())
                .build();
    }
}
