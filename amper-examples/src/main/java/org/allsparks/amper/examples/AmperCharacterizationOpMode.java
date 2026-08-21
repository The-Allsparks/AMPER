package org.allsparks.amper.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.ftc.AmperFtc;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;

/**
 * Hardware characterization OpMode. Ready for Control Hub execution, not
 * hardware-validated. Collects loop overhead for:
 * <ul>
 *   <li>AMPER disabled</li>
 *   <li>voltage only</li>
 *   <li>voltage + one motor current every loop</li>
 *   <li>voltage + N motor currents every loop</li>
 *   <li>voltage + N motors round-robin</li>
 * </ul>
 * Does not command motors and must not be used to induce a brownout.
 */
@TeleOp(name = "AMPER Characterization", group = "AMPER")
@Disabled
public class AmperCharacterizationOpMode extends OpMode {
    public enum Mode {
        DISABLED,
        VOLTAGE_ONLY,
        VOLTAGE_PLUS_ONE_CURRENT,
        VOLTAGE_PLUS_N_EVERY_LOOP,
        VOLTAGE_PLUS_N_ROUND_ROBIN
    }

    private Mode mode = Mode.VOLTAGE_ONLY;
    private AmperSession amper;
    private long loops;
    private long sumNs;
    private long maxNs;

    @Override
    public void init() {
        telemetry.addLine("AMPER characterization is NOT hardware-validated.");
        telemetry.addLine("Set mode in code. Do not induce a brownout.");
        telemetry.addData("mode", mode);
        telemetry.update();
        amper = build(mode);
        amper.initialize();
    }

    @Override
    public void start() {
        amper.start();
        loops = 0;
        sumNs = 0;
        maxNs = 0;
    }

    @Override
    public void loop() {
        ElectricalObservation obs = amper.observe();
        long ns = obs.loopDurationNanos();
        loops++;
        sumNs += ns;
        if (ns > maxNs) {
            maxNs = ns;
        }
        telemetry.addData("mode", mode);
        telemetry.addData("loops", loops);
        telemetry.addData("amperUs", ns / 1000L);
        telemetry.addData("meanUs", loops == 0 ? 0 : (sumNs / loops) / 1000L);
        telemetry.addData("maxUs", maxNs / 1000L);
        telemetry.addData("valid", obs.sensingValid());
        telemetry.addData("V", obs.filteredVoltage().volts());
        telemetry.update();
    }

    @Override
    public void stop() {
        amper.stop();
        amper.close();
    }

    private AmperSession build(Mode selected) {
        AmperFtc.Builder builder = AmperFtc.builder(hardwareMap).controlHubVoltage();
        if (selected == Mode.DISABLED) {
            return builder.policy(AmperPolicies.disabled()).persistLogs(false).build();
        }
        if (selected == Mode.VOLTAGE_ONLY) {
            return builder.policy(AmperPolicies.measurementOnly())
                    .exportFilename("amper-char-voltage.csv")
                    .build();
        }
        DcMotorEx m0 = hardwareMap.get(DcMotorEx.class, "frontLeft");
        builder.observeMotor("frontLeft", m0);
        if (selected == Mode.VOLTAGE_PLUS_ONE_CURRENT) {
            PowerPolicy policy = PowerPolicy.builder()
                    .featureFlags(org.allsparks.amper.AmperFeatureFlags.defaults())
                    .sampling(SamplingPolicy.everyLoop())
                    .build();
            return builder.policy(policy)
                    .exportFilename("amper-char-one-current.csv")
                    .build();
        }
        DcMotorEx m1 = hardwareMap.get(DcMotorEx.class, "frontRight");
        DcMotorEx m2 = hardwareMap.get(DcMotorEx.class, "backLeft");
        DcMotorEx m3 = hardwareMap.get(DcMotorEx.class, "backRight");
        builder.observeMotor("frontRight", m1).observeMotor("backLeft", m2).observeMotor("backRight", m3);
        SamplingPolicy sampling =
                selected == Mode.VOLTAGE_PLUS_N_EVERY_LOOP ? SamplingPolicy.everyLoop() : SamplingPolicy.recommended();
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(org.allsparks.amper.AmperFeatureFlags.defaults())
                .sampling(sampling)
                .build();
        String file = selected == Mode.VOLTAGE_PLUS_N_EVERY_LOOP
                ? "amper-char-n-every-loop.csv"
                : "amper-char-n-round-robin.csv";
        return builder.policy(policy).exportFilename(file).build();
    }
}
