package com.qualcomm.robotcore.hardware;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/** Compile-only stub matching FTC {@code DcMotorEx}. */
public interface DcMotorEx extends DcMotor {
    double getVelocity();

    void setVelocity(double angularRate);

    double getCurrent(CurrentUnit unit);
}
