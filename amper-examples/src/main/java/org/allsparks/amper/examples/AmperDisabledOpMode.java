package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.policy.AmperPolicies;

/**
 * AMPER fully disabled. Existing motor code is unchanged; observe() is a no-op
 * for hardware reads.
 */
@TeleOp(name = "AMPER Disabled", group = "AMPER")
@Disabled
public class AmperDisabledOpMode extends OpMode {
    private AmperSession amper;

    @Override
    public void init() {
        amper = AmperFtc.builder(hardwareMap)
                .controlHubVoltage()
                .policy(AmperPolicies.disabled())
                .persistLogs(false)
                .build();
        amper.initialize();
    }

    @Override
    public void loop() {
        amper.observe();
        telemetry.addData("AMPER", "disabled");
        telemetry.update();
    }

    @Override
    public void stop() {
        amper.stop();
        amper.close();
    }
}
