package org.allsparks.amper.ftc;

/**
 * Per-motor observation options. Current polling can be disabled when the
 * channel is unsupported or too expensive.
 */
public final class MotorObserveOptions {
    private final boolean pollCurrent;
    private final FtcMotorTelemetry.CommandSource commandSource;

    private MotorObserveOptions(boolean pollCurrent, FtcMotorTelemetry.CommandSource commandSource) {
        this.pollCurrent = pollCurrent;
        this.commandSource = commandSource;
    }

    public static MotorObserveOptions defaults() {
        return new MotorObserveOptions(true, null);
    }

    public static MotorObserveOptions withoutCurrent() {
        return new MotorObserveOptions(false, null);
    }

    public MotorObserveOptions commandSource(FtcMotorTelemetry.CommandSource source) {
        return new MotorObserveOptions(pollCurrent, source);
    }

    public boolean pollCurrent() {
        return pollCurrent;
    }

    public FtcMotorTelemetry.CommandSource commandSource() {
        return commandSource;
    }
}
