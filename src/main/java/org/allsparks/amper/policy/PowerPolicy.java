package org.allsparks.amper.policy;

import org.allsparks.amper.AmperFeatureFlags;

/**
 * Central configuration for thresholds, hysteresis, priorities, and feature
 * flags. Keep policy data here rather than scattering constants through
 * subsystem code.
 */
public final class PowerPolicy {
    private final AmperFeatureFlags featureFlags;
    private final double watchVoltageVolts;
    private final double limitingVoltageVolts;
    private final double criticalVoltageVolts;
    private final double recoveryVoltageVolts;
    private final long recoveryHoldNanos;
    private final double voltageFilterAlpha;
    private final long staleAfterNanos;
    private final double minValidVolts;
    private final double maxValidVolts;
    private final long telemetryMinPeriodNanos;

    private PowerPolicy(Builder builder) {
        this.featureFlags = builder.featureFlags;
        this.watchVoltageVolts = builder.watchVoltageVolts;
        this.limitingVoltageVolts = builder.limitingVoltageVolts;
        this.criticalVoltageVolts = builder.criticalVoltageVolts;
        this.recoveryVoltageVolts = builder.recoveryVoltageVolts;
        this.recoveryHoldNanos = builder.recoveryHoldNanos;
        this.voltageFilterAlpha = builder.voltageFilterAlpha;
        this.staleAfterNanos = builder.staleAfterNanos;
        this.minValidVolts = builder.minValidVolts;
        this.maxValidVolts = builder.maxValidVolts;
        this.telemetryMinPeriodNanos = builder.telemetryMinPeriodNanos;
    }

    public static PowerPolicy defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public AmperFeatureFlags featureFlags() {
        return featureFlags;
    }

    public double watchVoltageVolts() {
        return watchVoltageVolts;
    }

    public double limitingVoltageVolts() {
        return limitingVoltageVolts;
    }

    public double criticalVoltageVolts() {
        return criticalVoltageVolts;
    }

    public double recoveryVoltageVolts() {
        return recoveryVoltageVolts;
    }

    public long recoveryHoldNanos() {
        return recoveryHoldNanos;
    }

    public double voltageFilterAlpha() {
        return voltageFilterAlpha;
    }

    public long staleAfterNanos() {
        return staleAfterNanos;
    }

    public double minValidVolts() {
        return minValidVolts;
    }

    public double maxValidVolts() {
        return maxValidVolts;
    }

    public long telemetryMinPeriodNanos() {
        return telemetryMinPeriodNanos;
    }

    public static final class Builder {
        private AmperFeatureFlags featureFlags = AmperFeatureFlags.defaults();
        // Conservative placeholders for later phases; unused while intervention is off.
        private double watchVoltageVolts = 11.0;
        private double limitingVoltageVolts = 10.5;
        private double criticalVoltageVolts = 9.5;
        private double recoveryVoltageVolts = 11.2;
        private long recoveryHoldNanos = 250_000_000L;
        private double voltageFilterAlpha = 0.35;
        private long staleAfterNanos = 100_000_000L;
        private double minValidVolts = 5.0;
        private double maxValidVolts = 16.0;
        private long telemetryMinPeriodNanos = 100_000_000L;

        public Builder featureFlags(AmperFeatureFlags featureFlags) {
            this.featureFlags = featureFlags;
            return this;
        }

        public Builder watchVoltageVolts(double value) {
            this.watchVoltageVolts = value;
            return this;
        }

        public Builder limitingVoltageVolts(double value) {
            this.limitingVoltageVolts = value;
            return this;
        }

        public Builder criticalVoltageVolts(double value) {
            this.criticalVoltageVolts = value;
            return this;
        }

        public Builder recoveryVoltageVolts(double value) {
            this.recoveryVoltageVolts = value;
            return this;
        }

        public Builder recoveryHoldNanos(long value) {
            this.recoveryHoldNanos = value;
            return this;
        }

        public Builder voltageFilterAlpha(double value) {
            this.voltageFilterAlpha = value;
            return this;
        }

        public Builder staleAfterNanos(long value) {
            this.staleAfterNanos = value;
            return this;
        }

        public Builder minValidVolts(double value) {
            this.minValidVolts = value;
            return this;
        }

        public Builder maxValidVolts(double value) {
            this.maxValidVolts = value;
            return this;
        }

        public Builder telemetryMinPeriodNanos(long value) {
            this.telemetryMinPeriodNanos = value;
            return this;
        }

        public PowerPolicy build() {
            if (featureFlags == null) {
                throw new IllegalStateException("featureFlags required");
            }
            return new PowerPolicy(this);
        }
    }
}
