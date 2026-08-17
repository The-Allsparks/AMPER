package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.AmperPolicies;

/**
 * Multi-hub voltage observation. Replace the Expansion Hub name with your
 * configuration name. Does not command motors.
 */
@TeleOp(name = "AMPER Multi-Hub", group = "AMPER")
@Disabled
public class AmperMultiHubOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .expansionHubVoltage("Expansion Hub 1")
                .policy(AmperPolicies.passiveDefaults())
                .exportFilename("amper-multi-hub.csv")
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
