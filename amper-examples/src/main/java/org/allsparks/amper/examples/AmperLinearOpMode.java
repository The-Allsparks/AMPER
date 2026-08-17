package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.ftc.MotorObserveOptions;
import org.allsparks.amper.policy.AmperPolicies;

/**
 * LinearOpMode with bounded logging, Phase 1 warnings, and export on stop.
 * Does not command motors.
 */
@TeleOp(name = "AMPER Linear", group = "AMPER")
@Disabled
public class AmperLinearOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        DcMotorEx intake = hardwareMap.get(DcMotorEx.class, "intake");
        AmperSession amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .observeMotor("intake", intake, MotorObserveOptions.defaults())
                .policy(AmperPolicies.passiveDefaults())
                .exportFilename("amper-linear.csv")
                .build();
        amper.initialize();
        waitForStart();
        amper.start();
        while (opModeIsActive()) {
            amper.observe();
            amper.publishTelemetry(AmperFtc.telemetrySink(telemetry));
            idle();
        }
        amper.stop();
        amper.close();
    }
}
