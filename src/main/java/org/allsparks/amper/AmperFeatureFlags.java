package org.allsparks.amper;

/**
 * Central feature flags. Intervention-related flags default to {@code false}.
 *
 * <p>Missing or invalid measurements must fail safe: intervention remains off
 * and sensing is reported as invalid rather than inventing values.
 */
public final class AmperFeatureFlags {

    private final boolean phase0Measurement;
    private final boolean phase1PassiveTelemetry;
    private final boolean phase2LocalProtection;
    private final boolean phase3ReactiveVoltage;
    private final boolean phase4Coordination;
    private final boolean phase5PredictiveEstimate;
    private final boolean phase5ShadowOnly;
    private final boolean phase6PredictiveShaping;
    private final boolean phase7Adaptive;

    private AmperFeatureFlags(Builder builder) {
        this.phase0Measurement = builder.phase0Measurement;
        this.phase1PassiveTelemetry = builder.phase1PassiveTelemetry;
        this.phase2LocalProtection = builder.phase2LocalProtection;
        this.phase3ReactiveVoltage = builder.phase3ReactiveVoltage;
        this.phase4Coordination = builder.phase4Coordination;
        this.phase5PredictiveEstimate = builder.phase5PredictiveEstimate;
        this.phase5ShadowOnly = builder.phase5ShadowOnly;
        this.phase6PredictiveShaping = builder.phase6PredictiveShaping;
        this.phase7Adaptive = builder.phase7Adaptive;
    }

    /** Safe defaults: Phase 0 on; all intervention off. */
    public static AmperFeatureFlags defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isPhase0Measurement() {
        return phase0Measurement;
    }

    public boolean isPhase1PassiveTelemetry() {
        return phase1PassiveTelemetry;
    }

    public boolean isPhase2LocalProtection() {
        return phase2LocalProtection;
    }

    public boolean isPhase3ReactiveVoltage() {
        return phase3ReactiveVoltage;
    }

    public boolean isPhase4Coordination() {
        return phase4Coordination;
    }

    public boolean isPhase5PredictiveEstimate() {
        return phase5PredictiveEstimate;
    }

    public boolean isPhase5ShadowOnly() {
        return phase5ShadowOnly;
    }

    public boolean isPhase6PredictiveShaping() {
        return phase6PredictiveShaping;
    }

    public boolean isPhase7Adaptive() {
        return phase7Adaptive;
    }

    /** True if any feature that may constrain motor output is enabled. */
    public boolean isAnyInterventionEnabled() {
        return phase2LocalProtection
                || phase3ReactiveVoltage
                || phase4Coordination
                || (phase5PredictiveEstimate && !phase5ShadowOnly)
                || phase6PredictiveShaping
                || phase7Adaptive;
    }

    public static final class Builder {
        private boolean phase0Measurement = true;
        private boolean phase1PassiveTelemetry = false;
        private boolean phase2LocalProtection = false;
        private boolean phase3ReactiveVoltage = false;
        private boolean phase4Coordination = false;
        private boolean phase5PredictiveEstimate = false;
        private boolean phase5ShadowOnly = true;
        private boolean phase6PredictiveShaping = false;
        private boolean phase7Adaptive = false;

        public Builder phase0Measurement(boolean value) {
            this.phase0Measurement = value;
            return this;
        }

        public Builder phase1PassiveTelemetry(boolean value) {
            this.phase1PassiveTelemetry = value;
            return this;
        }

        public Builder phase2LocalProtection(boolean value) {
            this.phase2LocalProtection = value;
            return this;
        }

        public Builder phase3ReactiveVoltage(boolean value) {
            this.phase3ReactiveVoltage = value;
            return this;
        }

        public Builder phase4Coordination(boolean value) {
            this.phase4Coordination = value;
            return this;
        }

        public Builder phase5PredictiveEstimate(boolean value) {
            this.phase5PredictiveEstimate = value;
            return this;
        }

        public Builder phase5ShadowOnly(boolean value) {
            this.phase5ShadowOnly = value;
            return this;
        }

        public Builder phase6PredictiveShaping(boolean value) {
            this.phase6PredictiveShaping = value;
            return this;
        }

        public Builder phase7Adaptive(boolean value) {
            this.phase7Adaptive = value;
            return this;
        }

        public AmperFeatureFlags build() {
            return new AmperFeatureFlags(this);
        }
    }
}
