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
    private final double mechanismStartEffort;
    private final double mechanismStopEffort;
    private final double stallCurrentAmps;
    private final double stallVelocityTicksPerSecond;
    private final long stallDwellNanos;
    private final double weakBatterySagVolts;
    private final int loggerCapacity;

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
        this.mechanismStartEffort = builder.mechanismStartEffort;
        this.mechanismStopEffort = builder.mechanismStopEffort;
        this.stallCurrentAmps = builder.stallCurrentAmps;
        this.stallVelocityTicksPerSecond = builder.stallVelocityTicksPerSecond;
        this.weakBatterySagVolts = builder.weakBatterySagVolts;
        this.stallDwellNanos = builder.stallDwellNanos;
        this.loggerCapacity = builder.loggerCapacity;
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

    public double mechanismStartEffort() {
        return mechanismStartEffort;
    }

    public double mechanismStopEffort() {
        return mechanismStopEffort;
    }

    public double stallCurrentAmps() {
        return stallCurrentAmps;
    }

    public double stallVelocityTicksPerSecond() {
        return stallVelocityTicksPerSecond;
    }

    public long stallDwellNanos() {
        return stallDwellNanos;
    }

    public double weakBatterySagVolts() {
        return weakBatterySagVolts;
    }

    public int loggerCapacity() {
        return loggerCapacity;
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
        private double mechanismStartEffort = 0.10;
        private double mechanismStopEffort = 0.05;
        private double stallCurrentAmps = 8.0;
        private double stallVelocityTicksPerSecond = 50.0;
        private long stallDwellNanos = 150_000_000L;
        private double weakBatterySagVolts = 1.5;
        private int loggerCapacity = 4000;

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

        public Builder mechanismStartEffort(double value) {
            this.mechanismStartEffort = value;
            return this;
        }

        public Builder mechanismStopEffort(double value) {
            this.mechanismStopEffort = value;
            return this;
        }

        public Builder stallCurrentAmps(double value) {
            this.stallCurrentAmps = value;
            return this;
        }

        public Builder stallVelocityTicksPerSecond(double value) {
            this.stallVelocityTicksPerSecond = value;
            return this;
        }

        public Builder stallDwellNanos(long value) {
            this.stallDwellNanos = value;
            return this;
        }

        public Builder weakBatterySagVolts(double value) {
            this.weakBatterySagVolts = value;
            return this;
        }

        public Builder loggerCapacity(int value) {
            this.loggerCapacity = value;
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
