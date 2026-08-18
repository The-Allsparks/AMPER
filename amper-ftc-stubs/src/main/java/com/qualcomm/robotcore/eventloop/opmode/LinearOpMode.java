package com.qualcomm.robotcore.eventloop.opmode;

/** Compile-only stub matching FTC {@code LinearOpMode}. */
public abstract class LinearOpMode extends OpMode {
    public abstract void runOpMode() throws InterruptedException;

    public boolean opModeIsActive() {
        return true;
    }

    public void waitForStart() {
    }

    public void idle() {
    }

    @Override
    public final void init() {
    }

    @Override
    public final void loop() {
    }
}
