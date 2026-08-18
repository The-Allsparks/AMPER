package org.allsparks.amper.ftc;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.log.SessionMetadata;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * First-class FTC integration. Teams should not hand-wire Java suppliers.
 *
 * <pre>{@code
 * AmperSession amper = AmperFtc.builder(hardwareMap)
 *     .controlHubVoltage()
 *     .expansionHubVoltage("Expansion Hub 1")
 *     .observeMotor("frontLeft", frontLeft)
 *     .policy(AmperPolicies.passiveDefaults())
 *     .build();
 * }</pre>
 */
public final class AmperFtc {
    private AmperFtc() {
    }

    public static Builder builder(HardwareMap hardwareMap) {
        return new Builder(hardwareMap);
    }

    public static FtcTelemetrySink telemetrySink(Telemetry telemetry) {
        return new FtcTelemetrySink(telemetry);
    }

    public static final class Builder {
        private final HardwareMap hardwareMap;
        private final List<PowerTelemetrySource> voltages = new ArrayList<PowerTelemetrySource>();
        private final List<MotorElectricalTelemetry> motors = new ArrayList<MotorElectricalTelemetry>();
        private int policySourceIndex = 0;
        private boolean policySourceSet;
        private PowerPolicy policy = AmperPolicies.passiveDefaults();
        private String sessionId = "amper-session";
        private String exportFilename = "amper-session.csv";
        private boolean persistLogs = true;

        private Builder(HardwareMap hardwareMap) {
            this.hardwareMap = Objects.requireNonNull(hardwareMap, "hardwareMap");
        }

        /** Unique Control Hub voltage sensor (name contains "Control Hub"). */
        public Builder controlHubVoltage() {
            VoltageSensorDiscovery.NamedSensor found =
                    VoltageSensorDiscovery.requireUniqueContaining(hardwareMap, "Control Hub");
            voltages.add(new FtcVoltageSource(found.name, found.sensor));
            if (!policySourceSet) {
                policySourceIndex = voltages.size() - 1;
            }
            return this;
        }

        /** Explicit Expansion Hub (or other) voltage sensor by configured name. */
        public Builder expansionHubVoltage(String deviceName) {
            VoltageSensor sensor = VoltageSensorDiscovery.requireNamed(hardwareMap, deviceName);
            voltages.add(new FtcVoltageSource(deviceName, sensor));
            return this;
        }

        /** Explicit labeled voltage sensor. */
        public Builder voltageSensor(String label, String deviceName) {
            VoltageSensor sensor = VoltageSensorDiscovery.requireNamed(hardwareMap, deviceName);
            voltages.add(new FtcVoltageSource(label == null ? deviceName : label, sensor));
            return this;
        }

        /** Which labeled source drives filters and Phase 1 warnings. */
        public Builder policySourceIndex(int index) {
            this.policySourceIndex = index;
            this.policySourceSet = true;
            return this;
        }

        public Builder observeMotor(String name, DcMotorEx motor) {
            return observeMotor(name, motor, MotorObserveOptions.defaults());
        }

        public Builder observeMotor(String name, DcMotorEx motor, MotorObserveOptions options) {
            MotorObserveOptions opts = options == null ? MotorObserveOptions.defaults() : options;
            motors.add(new FtcMotorTelemetry(name, motor, opts.commandSource(), opts.pollCurrent()));
            return this;
        }

        public Builder policy(PowerPolicy policy) {
            this.policy = Objects.requireNonNull(policy, "policy");
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder exportFilename(String exportFilename) {
            this.exportFilename = exportFilename;
            return this;
        }

        public Builder persistLogs(boolean persistLogs) {
            this.persistLogs = persistLogs;
            return this;
        }

        public AmperSession build() {
            if (voltages.isEmpty()) {
                throw new IllegalStateException(
                        "No voltage source registered. Call controlHubVoltage() or voltageSensor(...). "
                                + "Available: " + VoltageSensorDiscovery.names(hardwareMap));
            }
            if (policySourceIndex < 0 || policySourceIndex >= voltages.size()) {
                throw new IllegalArgumentException("policySourceIndex out of range");
            }
            Map<String, String> extra = new LinkedHashMap<String, String>();
            extra.put("hubCount", Integer.toString(voltages.size()));
            extra.put("hardwarePlatform", "FTC_REV_HUB");
            SessionMetadata metadata = new SessionMetadata(
                    sessionId,
                    "",
                    policy.voltageThresholdProvenance().name(),
                    extra);
            FtcSessionLogSink sink = persistLogs ? new FtcSessionLogSink(hardwareMap.appContext) : null;
            return new AmperSession(
                    policy,
                    null,
                    voltages,
                    policySourceIndex,
                    motors,
                    metadata,
                    sink,
                    exportFilename);
        }
    }
}
