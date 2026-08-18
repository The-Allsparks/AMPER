package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.allsparks.amper.AmperFeatureFlags;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;

/**
 * Phase 1 disabled while Phase 0 measurement remains on. Warnings are off;
 * motors are still never commanded.
 */
@TeleOp(name = "AMPER Phase 1 Off", group = "AMPER")
@Disabled
public class AmperPhase1DisabledOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.defaults())
                .sampling(SamplingPolicy.recommended())
                .build();
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .policy(policy)
                .exportFilename("amper-phase1-off.csv")
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
