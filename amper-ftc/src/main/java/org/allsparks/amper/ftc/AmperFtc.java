package org.allsparks.amper.ftc;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.adapters.rev.RevHubTelemetrySource;
import org.allsparks.amper.input.AmperSignals;
import org.allsparks.amper.input.InputValuesMotorTelemetry;
import org.allsparks.amper.input.InputValuesPowerSource;
import org.allsparks.amper.input.OvercurrentFollowUp;
import org.allsparks.amper.log.SessionMetadata;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.observe.ElectricalObservationSink;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.contracts.input.InputDemand;
import org.allsparks.contracts.input.InputPriority;
import org.allsparks.contracts.input.InputRegistrar;
import org.allsparks.contracts.input.InputRequirements;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.SamplingPolicy;
import org.allsparks.contracts.input.SignalKey;
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
 *
 * <p>When a sampler owns Hub I/O (PULSE implements {@link InputRegistrar} and
 * {@link InputValues}), declare then bind then read:
 *
 * <pre>{@code
 * AmperFtc.Builder amper = AmperFtc.builder(hardwareMap).controlHubVoltage();
 * amper.declareInputs(pulse);
 * for (AmperFtc.PhysicalVoltage physical : amper.physicalVoltages()) {
 *     pulse.bindDouble(physical.key(), physical.getter());
 * }
 * AmperSession session = amper.readFrom(pulse).requestThrough(pulse).build();
 * }</pre>
 *
 * AMPER does not import {@code org.allsparks.pulse}. The raw
 * {@link #controlHubVoltage()} path still calls {@code VoltageSensor.getVoltage()}
 * when {@link Builder#readFrom(InputValues)} is not used.
 */
public final class AmperFtc {
    private AmperFtc() {}

    public static Builder builder(HardwareMap hardwareMap) {
        return new Builder(hardwareMap);
    }

    public static FtcTelemetrySink telemetrySink(Telemetry telemetry) {
        return new FtcTelemetrySink(telemetry);
    }

    /**
     * One voltage channel AMPER will observe, plus the physical getter a
     * sampler must bind. TeamCode binds; AMPER does not, so this library never
     * depends on PULSE.
     */
    public static final class PhysicalVoltage {
        private final SignalKey<Double> key;
        private final String sourceName;
        private final VoltageSensor sensor;
        private final DoubleSupplier getter;

        PhysicalVoltage(SignalKey<Double> key, String sourceName, VoltageSensor sensor, DoubleSupplier getter) {
            this.key = Objects.requireNonNull(key, "key");
            this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
            this.sensor = sensor;
            this.getter = Objects.requireNonNull(getter, "getter");
        }

        public SignalKey<Double> key() {
            return key;
        }

        public String sourceName() {
            return sourceName;
        }

        public DoubleSupplier getter() {
            return getter;
        }
    }

    public static final class Builder {
        private final HardwareMap hardwareMap;
        private final List<PhysicalVoltage> physicals = new ArrayList<PhysicalVoltage>();
        private final InputRequirements requirements = InputRequirements.create();
        private final List<MotorElectricalTelemetry> motors = new ArrayList<MotorElectricalTelemetry>();
        private int policySourceIndex = 0;
        private boolean policySourceSet;
        private PowerPolicy policy = AmperPolicies.passiveDefaults();
        private String sessionId = "amper-session";
        private String exportFilename = "amper-session.csv";
        private boolean persistLogs = true;
        private ElectricalObservationSink observationSink = ElectricalObservationSink.NOOP;
        private boolean declared;
        private InputValues inputValues;
        private InputDemand inputDemand;
        private final List<String> watchedMotors = new ArrayList<String>();

        private Builder(HardwareMap hardwareMap) {
            this.hardwareMap = Objects.requireNonNull(hardwareMap, "hardwareMap");
        }

        /** Unique Control Hub voltage sensor (name contains "Control Hub"). */
        public Builder controlHubVoltage() {
            VoltageSensorDiscovery.NamedSensor found =
                    VoltageSensorDiscovery.requireUniqueContaining(hardwareMap, "Control Hub");
            addPhysical(AmperSignals.CONTROL_HUB_VOLTAGE, found.name, found.sensor, found.sensor::getVoltage, true);
            return this;
        }

        /** Explicit Expansion Hub (or other) voltage sensor by configured name. */
        public Builder expansionHubVoltage(String deviceName) {
            VoltageSensor sensor = VoltageSensorDiscovery.requireNamed(hardwareMap, deviceName);
            addPhysical(AmperSignals.hubVoltage(deviceName), deviceName, sensor, sensor::getVoltage, false);
            return this;
        }

        /** Explicit labeled voltage sensor. */
        public Builder voltageSensor(String label, String deviceName) {
            VoltageSensor sensor = VoltageSensorDiscovery.requireNamed(hardwareMap, deviceName);
            String name = label == null ? deviceName : label;
            addPhysical(AmperSignals.hubVoltage(name), name, sensor, sensor::getVoltage, false);
            return this;
        }

        /**
         * Bus voltage from an already-sampled supplier (for example a test
         * double). Does not call {@code VoltageSensor.getVoltage()}. AMPER
         * still does not depend on PULSE. Prefer {@link #declareInputs} plus
         * {@link #readFrom} when a sampler owns the Hub read.
         */
        public Builder busVoltage(String label, DoubleSupplier volts) {
            Objects.requireNonNull(volts, "volts");
            String name = label == null || label.trim().isEmpty() ? "bus" : label;
            addPhysical(AmperSignals.hubVoltage(name), name, null, volts, true);
            return this;
        }

        /**
         * Register AMPER voltage keys on a sampler. Call before freeze. Pair
         * with {@link #readFrom(InputValues)} so {@code observe()} does not
         * call {@code getVoltage()} a second time.
         */
        public Builder declareInputs(InputRegistrar registrar) {
            requirements.registerWith(Objects.requireNonNull(registrar, "registrar"));
            declared = true;
            return this;
        }

        /**
         * Observe published samples instead of the hardware sensor. Must follow
         * {@link #declareInputs(InputRegistrar)}. The values object must be the
         * same sampler that captured this loop.
         */
        public Builder readFrom(InputValues values) {
            this.inputValues = Objects.requireNonNull(values, "values");
            return this;
        }

        /**
         * Loop-time demand for on-demand current after an over-current flag.
         * Pair with {@link #readFrom(InputValues)} and {@link #watchMotor(String)}.
         * The demand object must be the same sampler that captures this loop.
         */
        public Builder requestThrough(InputDemand demand) {
            this.inputDemand = Objects.requireNonNull(demand, "demand");
            return this;
        }

        /**
         * Watch a named motor's bulk over-current flag and record one
         * {@code getCurrent} sample on the next capture after a trip. Does not
         * call Hub current itself. Requires {@link #declareInputs},
         * {@link #readFrom}, and {@link #requestThrough}.
         */
        public Builder watchMotor(String deviceName) {
            if (deviceName == null) {
                throw new IllegalArgumentException("deviceName is required");
            }
            String trimmed = deviceName.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("deviceName must not be blank");
            }
            watchedMotors.add(trimmed);
            requirements.require(AmperSignals.overCurrent(trimmed), SamplingPolicy.everyCycle(), InputPriority.NORMAL);
            requirements.require(AmperSignals.motorCurrent(trimmed), SamplingPolicy.onDemand(), InputPriority.NORMAL);
            return this;
        }

        /**
         * Physical getters TeamCode must bind on the sampler after
         * {@link #declareInputs(InputRegistrar)} and before freeze.
         */
        public List<PhysicalVoltage> physicalVoltages() {
            return Collections.unmodifiableList(physicals);
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

        /**
         * TRACE adapter or tests. NOOP default. AMPER still does not import TRACE.
         */
        public Builder observationSink(ElectricalObservationSink observationSink) {
            this.observationSink = observationSink == null ? ElectricalObservationSink.NOOP : observationSink;
            return this;
        }

        public AmperSession build() {
            if (physicals.isEmpty()) {
                throw new IllegalStateException(
                        "No voltage source registered. Call controlHubVoltage(), voltageSensor(...), or busVoltage(). "
                                + "Available: "
                                + VoltageSensorDiscovery.names(hardwareMap));
            }
            if (inputValues != null && !declared) {
                throw new IllegalStateException("readFrom(InputValues) requires declareInputs(InputRegistrar) first");
            }
            if (declared && inputValues == null) {
                throw new IllegalStateException(
                        "declareInputs(...) requires readFrom(InputValues) so AMPER does not call getVoltage() a second time");
            }
            if (!watchedMotors.isEmpty() && (inputValues == null || inputDemand == null)) {
                throw new IllegalStateException(
                        "watchMotor requires readFrom(InputValues) and requestThrough(InputDemand) so AMPER does not call getCurrent()");
            }
            if (inputDemand != null && inputValues == null) {
                throw new IllegalStateException("requestThrough(InputDemand) requires readFrom(InputValues)");
            }
            if (policySourceIndex < 0 || policySourceIndex >= physicals.size()) {
                throw new IllegalArgumentException("policySourceIndex out of range");
            }
            List<PowerTelemetrySource> voltages = new ArrayList<PowerTelemetrySource>(physicals.size());
            for (int i = 0; i < physicals.size(); i++) {
                voltages.add(sourceFor(physicals.get(i)));
            }
            Map<String, String> extra = new LinkedHashMap<String, String>();
            extra.put("hubCount", Integer.toString(voltages.size()));
            extra.put("hardwarePlatform", "FTC_REV_HUB");
            extra.put("inputPath", inputValues == null ? "direct" : "input-values");
            SessionMetadata metadata = new SessionMetadata(
                    sessionId, "", policy.voltageThresholdProvenance().name(), extra);
            FtcSessionLogSink sink = persistLogs ? new FtcSessionLogSink(hardwareMap.appContext) : null;
            List<MotorElectricalTelemetry> sessionMotors = new ArrayList<MotorElectricalTelemetry>(motors);
            OvercurrentFollowUp followUp = null;
            if (!watchedMotors.isEmpty()) {
                long staleAfter = policy.staleAfterNanos();
                for (int i = 0; i < watchedMotors.size(); i++) {
                    String name = watchedMotors.get(i);
                    sessionMotors.add(new InputValuesMotorTelemetry(
                            name, inputValues, AmperSignals.motorCurrent(name), staleAfter));
                }
                followUp = new OvercurrentFollowUp(inputValues, inputDemand, watchedMotors);
            }
            return new AmperSession(
                            policy, null, voltages, policySourceIndex, sessionMotors, metadata, sink, exportFilename)
                    .observationSink(observationSink)
                    .overcurrentFollowUp(followUp);
        }

        private void addPhysical(
                SignalKey<Double> key,
                String sourceName,
                VoltageSensor sensor,
                DoubleSupplier getter,
                boolean defaultPolicySource) {
            physicals.add(new PhysicalVoltage(key, sourceName, sensor, getter));
            requirements.require(key, SamplingPolicy.everyCycle(), InputPriority.NORMAL);
            if (defaultPolicySource && !policySourceSet) {
                policySourceIndex = physicals.size() - 1;
            }
        }

        private PowerTelemetrySource sourceFor(PhysicalVoltage physical) {
            if (inputValues != null) {
                return new InputValuesPowerSource(inputValues, physical.key(), physical.sourceName());
            }
            if (physical.sensor != null) {
                return new FtcVoltageSource(physical.sourceName(), physical.sensor);
            }
            return RevHubTelemetrySource.voltageOnly(physical.sourceName(), physical.getter());
        }
    }
}
