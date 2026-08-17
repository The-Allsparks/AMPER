package org.allsparks.amper.policy;

/**
 * Configurable sampling cadences. Voltage may be cheap; motor current is not.
 *
 * <p>Recommended robot defaults round-robin current so AMPER does not poll
 * every motor every loop.
 */
public final class SamplingPolicy {
    private final long voltagePeriodNanos;
    private final long currentPeriodNanos;
    private final long velocityPeriodNanos;
    private final long commandPeriodNanos;
    private final int maxCurrentReadsPerLoop;
    private final long duplicateObserveWindowNanos;

    private SamplingPolicy(Builder builder) {
        this.voltagePeriodNanos = builder.voltagePeriodNanos;
        this.currentPeriodNanos = builder.currentPeriodNanos;
        this.velocityPeriodNanos = builder.velocityPeriodNanos;
        this.commandPeriodNanos = builder.commandPeriodNanos;
        this.maxCurrentReadsPerLoop = builder.maxCurrentReadsPerLoop;
        this.duplicateObserveWindowNanos = builder.duplicateObserveWindowNanos;
    }

    /**
     * Sample voltage, command, velocity, and every motor current every update.
     * Intended for unit tests and characterization, not competition loops.
     */
    public static SamplingPolicy everyLoop() {
        return builder()
                .voltagePeriodNanos(0L)
                .currentPeriodNanos(0L)
                .velocityPeriodNanos(0L)
                .commandPeriodNanos(0L)
                .maxCurrentReadsPerLoop(Integer.MAX_VALUE)
                .build();
    }

    /**
     * Voltage every loop; at most one motor current read per loop, round-robin.
     * Not hardware-validated; a starting cadence for student characterization.
     */
    public static SamplingPolicy recommended() {
        return builder()
                .voltagePeriodNanos(0L)
                .currentPeriodNanos(0L)
                .velocityPeriodNanos(0L)
                .commandPeriodNanos(0L)
                .maxCurrentReadsPerLoop(1)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public long voltagePeriodNanos() {
        return voltagePeriodNanos;
    }

    public long currentPeriodNanos() {
        return currentPeriodNanos;
    }

    public long velocityPeriodNanos() {
        return velocityPeriodNanos;
    }

    public long commandPeriodNanos() {
        return commandPeriodNanos;
    }

    public int maxCurrentReadsPerLoop() {
        return maxCurrentReadsPerLoop;
    }

    public long duplicateObserveWindowNanos() {
        return duplicateObserveWindowNanos;
    }

    public static final class Builder {
        private long voltagePeriodNanos = 0L;
        private long currentPeriodNanos = 0L;
        private long velocityPeriodNanos = 0L;
        private long commandPeriodNanos = 0L;
        private int maxCurrentReadsPerLoop = 1;
        private long duplicateObserveWindowNanos = 1_000_000L;

        public Builder voltagePeriodNanos(long value) {
            this.voltagePeriodNanos = value;
            return this;
        }

        public Builder currentPeriodNanos(long value) {
            this.currentPeriodNanos = value;
            return this;
        }

        public Builder velocityPeriodNanos(long value) {
            this.velocityPeriodNanos = value;
            return this;
        }

        public Builder commandPeriodNanos(long value) {
            this.commandPeriodNanos = value;
            return this;
        }

        public Builder maxCurrentReadsPerLoop(int value) {
            this.maxCurrentReadsPerLoop = value;
            return this;
        }

        public Builder duplicateObserveWindowNanos(long value) {
            this.duplicateObserveWindowNanos = value;
            return this;
        }

        public SamplingPolicy build() {
            if (voltagePeriodNanos < 0L
                    || currentPeriodNanos < 0L
                    || velocityPeriodNanos < 0L
                    || commandPeriodNanos < 0L
                    || duplicateObserveWindowNanos < 0L) {
                throw new IllegalArgumentException("sampling periods must be nonnegative");
            }
            if (maxCurrentReadsPerLoop < 0) {
                throw new IllegalArgumentException("maxCurrentReadsPerLoop must be >= 0");
            }
            return new SamplingPolicy(this);
        }
    }
}
