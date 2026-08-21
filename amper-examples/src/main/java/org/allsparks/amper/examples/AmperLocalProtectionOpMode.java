package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.protect.ConstrainedCommand;
import org.allsparks.amper.protect.LocalProtection;

/**
 * Experimental Phase 2 pick-up path: observe passively, then optionally slew
 * drivetrain commands through {@link LocalProtection}.
 *
 * <p>Defaults stay safe until you copy this OpMode and remove {@link Disabled}.
 * Dual opt-in: {@link AmperPolicies#localProtectionAllowed()} opens the session
 * gate; {@link AmperSession#localProtection(boolean)} with {@code true} enables
 * local slew. AMPER still never calls {@code setPower} for you.
 *
 * <p>Do not use gravity-hold on elevators/arms from this example until issue #27
 * is resolved. Rename hardware map names to match your robot.
 */
@TeleOp(name = "AMPER LocalProtection (experimental)", group = "AMPER")
@Disabled
public class AmperLocalProtectionOpMode extends OpMode {
    private AmperSession amper;
    private LocalProtection leftProtection;
    private LocalProtection rightProtection;
    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .observeMotor("frontLeft", frontLeft)
                .observeMotor("frontRight", frontRight)
                .policy(AmperPolicies.localProtectionAllowed())
                .exportFilename("amper-local-protection.csv")
                .build();
        // One protection instance per motor so slew state is independent.
        // Kill switch demo: pass false (or use passiveDefaults()) to force identity.
        leftProtection = amper.localProtection(true);
        rightProtection = amper.localProtection(true);
        amper.initialize();
    }

    @Override
    public void start() {
        amper.start();
    }

    @Override
    public void loop() {
        amper.observe();

        double leftRequest = -gamepad1.left_stick_y;
        double rightRequest = -gamepad1.right_stick_y;
        ConstrainedCommand left = amper.constrain(leftProtection, leftRequest);
        ConstrainedCommand right = amper.constrain(rightProtection, rightRequest);
        frontLeft.setPower(left.allowed());
        frontRight.setPower(right.allowed());

        telemetry.addData("AMPER.P2", leftProtection.enabled() && leftProtection.sessionGateOpen());
        telemetry.addData("left.req", left.requested());
        telemetry.addData("left.allow", left.allowed());
        amper.publishTelemetry(AmperFtc.telemetrySink(telemetry));
    }

    @Override
    public void stop() {
        amper.stop();
        amper.close();
    }
}
