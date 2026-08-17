package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.ftc.MotorObserveOptions;
import org.allsparks.amper.policy.AmperPolicies;

/**
 * Graceful operation when motor current is unavailable. Voltage still works.
 */
@TeleOp(name = "AMPER Current Unavailable", group = "AMPER")
@Disabled
public class AmperCurrentUnavailableOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        DcMotorEx intake = hardwareMap.get(DcMotorEx.class, "intake");
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .observeMotor("intake", intake, MotorObserveOptions.withoutCurrent())
                .policy(AmperPolicies.passiveDefaults())
                .exportFilename("amper-no-current.csv")
                .build();
        amper.initialize();
    }

    @Override
    public void start() {
        amper.start();
    }

    @Override
    public void loop() {
        amper.observe();
        amper.publishTelemetry(AmperFtc.telemetrySink(telemetry));
    }

    @Override
    public void stop() {
        amper.stop();
        amper.close();
    }
}
