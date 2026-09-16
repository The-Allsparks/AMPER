package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.allsparks.amper.AmperFeatureFlags;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;
import org.allsparks.amper.policy.ThresholdProvenance;

/**
 * Voltage plus selected motor current. Rename hardware map names to match
 * your robot configuration. Does not command motors.
 */
@TeleOp(name = "AMPER Voltage + Motors", group = "AMPER")
@Disabled
public class AmperVoltageAndCurrentOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        DcMotorEx frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        DcMotorEx frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        DcMotorEx lift = hardwareMap.get(DcMotorEx.class, "lift");
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .observeMotor("frontLeft", frontLeft)
                .observeMotor("frontRight", frontRight)
                .observeMotor("lift", lift)
                .policy(PowerPolicy.builder()
                        .featureFlags(AmperFeatureFlags.passiveTelemetry())
                        .sampling(SamplingPolicy.recommended())
                        .voltageThresholdProvenance(ThresholdProvenance.CONSERVATIVE_PLACEHOLDER)
                        .build())
                .exportFilename("amper-voltage-motors.csv")
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
