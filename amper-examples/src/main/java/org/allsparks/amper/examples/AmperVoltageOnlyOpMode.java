package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.AmperPolicies;

/**
 * Voltage-only iterative OpMode. Copy into TeamCode and enable by removing
 * {@link Disabled}. Does not command motors.
 */
@TeleOp(name = "AMPER Voltage Only", group = "AMPER")
@Disabled
public class AmperVoltageOnlyOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .policy(AmperPolicies.passiveDefaults())
                .exportFilename("amper-voltage-only.csv")
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
