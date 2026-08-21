package org.allsparks.amper.policy;

import org.allsparks.amper.AmperFeatureFlags;

/**
 * Named policy presets. Voltage numbers remain conservative placeholders
 * until a team records Control Hub characterization.
 */
public final class AmperPolicies {
    private AmperPolicies() {}

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

    /**
     * Phase 0 + Phase 1, and opens the session gate for Phase 2
     * {@link org.allsparks.amper.protect.LocalProtection}.
     *
     * <p>Still does <strong>not</strong> wrap motors. A subsystem must also
     * construct protection with local {@code enabled(true)} and apply
     * {@link org.allsparks.amper.protect.ConstrainedCommand#allowed()} itself.
     * Experimental until Control Hub characterization exists.
     */
    public static PowerPolicy localProtectionAllowed() {
        return PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.builder()
                        .phase1PassiveTelemetry(true)
                        .phase2LocalProtection(true)
                        .build())
                .sampling(SamplingPolicy.recommended())
                .voltageThresholdProvenance(ThresholdProvenance.CONSERVATIVE_PLACEHOLDER)
                .build();
    }

    /** Fully disabled: {@link AmperFeatureFlags#isPhase0Measurement()} is false. */
    public static PowerPolicy disabled() {
        return PowerPolicy.builder()
                .featureFlags(
                        AmperFeatureFlags.builder().phase0Measurement(false).build())
                .sampling(SamplingPolicy.recommended())
                .build();
    }
}
