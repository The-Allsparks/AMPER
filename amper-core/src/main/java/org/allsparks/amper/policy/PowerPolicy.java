package org.allsparks.amper.policy;

import org.allsparks.amper.AmperFeatureFlags;

/**
 * Central configuration for thresholds, hysteresis, sampling, logging, and
 * feature flags.
 *
 * <p>Default voltage numbers are {@link ThresholdProvenance#CONSERVATIVE_PLACEHOLDER}
 * values for student starting points. They are not universal FTC truth and are
 * not hardware-validated.
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
    private final SamplingPolicy sampling;
    private final ThresholdProvenance voltageThresholdProvenance;
    private final double slewMaxDeltaPerSecond;
    private final double commandCap;
    private final boolean commandCapEnabled;

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
        this.sampling = builder.sampling;
        this.voltageThresholdProvenance = builder.voltageThresholdProvenance;
        this.slewMaxDeltaPerSecond = builder.slewMaxDeltaPerSecond;
        this.commandCap = builder.commandCap;
        this.commandCapEnabled = builder.commandCapEnabled;
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

    public SamplingPolicy sampling() {
        return sampling;
    }

    public ThresholdProvenance voltageThresholdProvenance() {
        return voltageThresholdProvenance;
    }

    public double slewMaxDeltaPerSecond() {
        return slewMaxDeltaPerSecond;
    }

    public double commandCap() {
        return commandCap;
    }

    public boolean commandCapEnabled() {
        return commandCapEnabled;
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
        private SamplingPolicy sampling = SamplingPolicy.everyLoop();
        private ThresholdProvenance voltageThresholdProvenance = ThresholdProvenance.CONSERVATIVE_PLACEHOLDER;
        private double slewMaxDeltaPerSecond = 4.0;
        private double commandCap = 1.0;
        private boolean commandCapEnabled = false;

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

        public Builder sampling(SamplingPolicy sampling) {
            this.sampling = sampling;
            return this;
        }

        public Builder voltageThresholdProvenance(ThresholdProvenance provenance) {
            this.voltageThresholdProvenance = provenance;
            return this;
        }

        public Builder slewMaxDeltaPerSecond(double value) {
            this.slewMaxDeltaPerSecond = value;
            return this;
        }

        public Builder commandCap(double value) {
            this.commandCap = value;
            this.commandCapEnabled = true;
            return this;
        }

        public Builder commandCapEnabled(boolean value) {
            this.commandCapEnabled = value;
            return this;
        }

        public PowerPolicy build() {
            if (featureFlags == null) {
                throw new IllegalStateException("featureFlags required");
            }
            if (sampling == null) {
                throw new IllegalStateException("sampling required");
            }
            if (voltageThresholdProvenance == null) {
                throw new IllegalStateException("voltageThresholdProvenance required");
            }
            requireFinite("watchVoltageVolts", watchVoltageVolts);
            requireFinite("limitingVoltageVolts", limitingVoltageVolts);
            requireFinite("criticalVoltageVolts", criticalVoltageVolts);
            requireFinite("recoveryVoltageVolts", recoveryVoltageVolts);
            requireFinite("voltageFilterAlpha", voltageFilterAlpha);
            requireFinite("minValidVolts", minValidVolts);
            requireFinite("maxValidVolts", maxValidVolts);
            requireFinite("mechanismStartEffort", mechanismStartEffort);
            requireFinite("mechanismStopEffort", mechanismStopEffort);
            requireFinite("stallCurrentAmps", stallCurrentAmps);
            requireFinite("stallVelocityTicksPerSecond", stallVelocityTicksPerSecond);
            requireFinite("weakBatterySagVolts", weakBatterySagVolts);
            requireFinite("slewMaxDeltaPerSecond", slewMaxDeltaPerSecond);
            requireFinite("commandCap", commandCap);
            if (!(criticalVoltageVolts < limitingVoltageVolts
                    && limitingVoltageVolts < watchVoltageVolts
                    && watchVoltageVolts <= recoveryVoltageVolts)) {
                throw new IllegalArgumentException(
                        "voltage thresholds must satisfy critical < limiting < watch <= recovery");
            }
            if (!(minValidVolts < maxValidVolts)) {
                throw new IllegalArgumentException("minValidVolts must be < maxValidVolts");
            }
            if (!(voltageFilterAlpha > 0.0) || voltageFilterAlpha > 1.0) {
                throw new IllegalArgumentException("voltageFilterAlpha must be in (0, 1]");
            }
            if (recoveryHoldNanos < 0L
                    || staleAfterNanos < 0L
                    || telemetryMinPeriodNanos < 0L
                    || stallDwellNanos < 0L) {
                throw new IllegalArgumentException("durations must be nonnegative");
            }
            if (staleAfterNanos == 0L) {
                throw new IllegalArgumentException("staleAfterNanos must be > 0 so freshness is defined");
            }
            if (loggerCapacity < 1 || loggerCapacity > 50_000) {
                throw new IllegalArgumentException("loggerCapacity must be in [1, 50000]");
            }
            if (mechanismStartEffort < 0.0
                    || mechanismStopEffort < 0.0
                    || mechanismStartEffort < mechanismStopEffort) {
                throw new IllegalArgumentException(
                        "effort thresholds must be nonnegative and start >= stop");
            }
            if (stallCurrentAmps < 0.0 || stallVelocityTicksPerSecond < 0.0 || weakBatterySagVolts < 0.0) {
                throw new IllegalArgumentException("current/velocity/sag thresholds must be nonnegative");
            }
            if (slewMaxDeltaPerSecond <= 0.0) {
                throw new IllegalArgumentException("slewMaxDeltaPerSecond must be > 0");
            }
            if (commandCap < 0.0) {
                throw new IllegalArgumentException("commandCap must be nonnegative");
            }
            return new PowerPolicy(this);
        }

        private static void requireFinite(String name, double value) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException(name + " must be finite");
            }
        }
    }
}
