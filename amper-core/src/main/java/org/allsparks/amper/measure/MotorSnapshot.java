package org.allsparks.amper.measure;

import java.util.Objects;

/** One motor's electrical and command snapshot. Never used to write hardware. */
public final class MotorSnapshot {
    private final String motorId;
    private final CurrentSample current;
    private final double commandedEffort;
    private final double velocityTicksPerSecond;
    private final double positionTicks;
    private final boolean currentReadThisLoop;
    private final boolean active;

    public MotorSnapshot(
            String motorId,
            CurrentSample current,
            double commandedEffort,
            double velocityTicksPerSecond,
            double positionTicks) {
        this(motorId, current, commandedEffort, velocityTicksPerSecond, positionTicks, true, false);
    }

    public MotorSnapshot(
            String motorId,
            CurrentSample current,
            double commandedEffort,
            double velocityTicksPerSecond,
            double positionTicks,
            boolean currentReadThisLoop,
            boolean active) {
        this.motorId = motorId == null ? "" : motorId;
        this.current = Objects.requireNonNull(current, "current");
        this.commandedEffort = commandedEffort;
        this.velocityTicksPerSecond = velocityTicksPerSecond;
        this.positionTicks = positionTicks;
        this.currentReadThisLoop = currentReadThisLoop;
        this.active = active;
    }

    public String motorId() {
        return motorId;
    }

    public CurrentSample current() {
        return current;
    }

    public double commandedEffort() {
        return commandedEffort;
    }

    public double velocityTicksPerSecond() {
        return velocityTicksPerSecond;
    }

    public double positionTicks() {
        return positionTicks;
    }

    public boolean currentReadThisLoop() {
        return currentReadThisLoop;
    }

    public boolean active() {
        return active;
    }
}
