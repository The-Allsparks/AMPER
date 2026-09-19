package org.allsparks.amper.ftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.policy.AmperPolicies;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;
import org.allsparks.contracts.input.InputPriority;
import org.allsparks.contracts.input.InputRegistrar;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.Sample;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.junit.jupiter.api.Test;

class AmperFtcIntegrationTest {
    @Test
    void builderUsesNamedControlHubAndDoesNotGuess() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.6));
        map.put("Expansion Hub 1", voltage(12.2));
        AmperSession session = AmperFtc.builder(map)
                .controlHubVoltage()
                .expansionHubVoltage("Expansion Hub 1")
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals("Control Hub", obs.rawVoltage().sourceId());
        assertEquals(2, obs.allVoltages().size());
        assertEquals(12.2, obs.allVoltages().get(1).volts(), 1e-9);
    }

    @Test
    void ambiguousControlHubIsRejected() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub A", voltage(12.0));
        map.put("Control Hub B", voltage(12.1));
        assertThrows(IllegalArgumentException.class, () -> AmperFtc.builder(map).controlHubVoltage());
    }

    @Test
    void missingExpansionNameIsRejected() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.0));
        assertThrows(
                IllegalArgumentException.class,
                () -> AmperFtc.builder(map).controlHubVoltage().expansionHubVoltage("Expansion Hub 9"));
    }

    @Test
    void motorSpyFailsIfOutputMethodsAreCalled() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.4));
        SpyMotor motor = new SpyMotor();
        map.put("frontLeft", motor);
        AmperSession session = AmperFtc.builder(map)
                .controlHubVoltage()
                .observeMotor("frontLeft", motor)
                // Characterization sampling: this test must actually call getCurrent.
                .policy(PowerPolicy.builder()
                        .sampling(SamplingPolicy.everyLoop())
                        .build())
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals(0.25, obs.motors().get(0).commandedEffort(), 1e-9);
        assertEquals(1.5, obs.motors().get(0).current().amps(), 1e-9);
        assertFalse(motor.wrote);
    }

    @Test
    void unsupportedCurrentStaysUnsupported() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.0));
        SpyMotor motor = new SpyMotor();
        motor.currentSupported = false;
        AmperSession session = AmperFtc.builder(map)
                .controlHubVoltage()
                .observeMotor("intake", motor, MotorObserveOptions.withoutCurrent())
                .policy(PowerPolicy.builder()
                        .sampling(SamplingPolicy.everyLoop())
                        .build())
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals(
                MeasurementValidity.UNSUPPORTED, obs.motors().get(0).current().validity());
        assertTrue(Double.isNaN(obs.motors().get(0).current().amps()));
    }

    @Test
    void busVoltageSupplierDoesNotRequireAHardwareSensor() {
        HardwareMap map = new HardwareMap();
        final int[] reads = {0};
        AmperSession session = AmperFtc.builder(map)
                .busVoltage("cached", () -> {
                    reads[0]++;
                    return 12.4;
                })
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals("cached", obs.rawVoltage().sourceId());
        assertEquals(12.4, obs.rawVoltage().volts(), 1e-9);
        assertEquals(1, reads[0]);
    }

    @Test
    void readFromUsesPublishedSampleAndDoesNotTouchTheSensor() {
        HardwareMap map = new HardwareMap();
        CountingVoltage sensor = new CountingVoltage(12.0);
        map.put("Control Hub", sensor);
        PublishedInputs inputs = new PublishedInputs(11.7);
        AmperSession session = AmperFtc.builder(map)
                .controlHubVoltage()
                .declareInputs(inputs)
                .readFrom(inputs)
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals(11.7, obs.rawVoltage().volts(), 1e-9);
        assertEquals("Control Hub", obs.rawVoltage().sourceId());
        assertEquals(0, sensor.reads);
        assertEquals(org.allsparks.amper.input.AmperSignals.CONTROL_HUB_VOLTAGE, inputs.lastRequired);
    }

    @Test
    void declareInputsWithoutReadFromIsRejected() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.0));
        PublishedInputs inputs = new PublishedInputs(12.0);
        assertThrows(IllegalStateException.class, () -> AmperFtc.builder(map)
                .controlHubVoltage()
                .declareInputs(inputs)
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build());
    }

    @Test
    void watchMotorWithoutDemandIsRejected() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.0));
        PublishedInputs inputs = new PublishedInputs(12.0);
        assertThrows(IllegalStateException.class, () -> AmperFtc.builder(map)
                .controlHubVoltage()
                .watchMotor("front_left_drive")
                .declareInputs(inputs)
                .readFrom(inputs)
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build());
    }

    @Test
    void readFromWithoutDeclareInputsIsRejected() {
        HardwareMap map = new HardwareMap();
        map.put("Control Hub", voltage(12.0));
        PublishedInputs inputs = new PublishedInputs(12.0);
        assertThrows(IllegalStateException.class, () -> AmperFtc.builder(map)
                .controlHubVoltage()
                .readFrom(inputs)
                .policy(AmperPolicies.measurementOnly())
                .persistLogs(false)
                .build());
    }

    private static VoltageSensor voltage(final double volts) {
        return new VoltageSensor() {
            @Override
            public double getVoltage() {
                return volts;
            }

            @Override
            public String getDeviceName() {
                return "voltage";
            }

            @Override
            public String getConnectionInfo() {
                return "";
            }
        };
    }

    private static final class CountingVoltage implements VoltageSensor {
        final double volts;
        int reads;

        CountingVoltage(double volts) {
            this.volts = volts;
        }

        @Override
        public double getVoltage() {
            reads++;
            return volts;
        }

        @Override
        public String getDeviceName() {
            return "voltage";
        }

        @Override
        public String getConnectionInfo() {
            return "";
        }
    }

    private static final class PublishedInputs implements InputRegistrar, InputValues {
        final double volts;
        SignalKey<?> lastRequired;

        PublishedInputs(double volts) {
            this.volts = volts;
        }

        @Override
        public void require(
                SignalKey<?> key, org.allsparks.contracts.input.SamplingPolicy policy, InputPriority priority) {
            lastRequired = key;
        }

        @Override
        public void requireGroup(
                String groupId,
                org.allsparks.contracts.input.SamplingPolicy policy,
                InputPriority priority,
                SignalKey<?>... members) {}

        @Override
        public long currentCycleId() {
            return 1L;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Sample<T> get(SignalKey<T> key) {
            return (Sample<T>) Sample.of(Double.valueOf(volts), Validity.VALID, 25L, 1L, true, null);
        }

        @Override
        public int getInt(SignalKey<Integer> key) {
            return 0;
        }

        @Override
        public long getLong(SignalKey<Long> key) {
            return 0L;
        }

        @Override
        public double getDouble(SignalKey<Double> key) {
            return volts;
        }

        @Override
        public Validity validity(SignalKey<?> key) {
            return Validity.VALID;
        }

        @Override
        public long captureTimestampNanos(SignalKey<?> key) {
            return 25L;
        }

        @Override
        public boolean getBoolean(SignalKey<Boolean> key) {
            return false;
        }
    }

    private static final class SpyMotor implements DcMotorEx {
        boolean wrote;
        boolean currentSupported = true;

        @Override
        public void setPower(double power) {
            wrote = true;
            fail("setPower must not be called");
        }

        @Override
        public void setVelocity(double angularRate) {
            wrote = true;
            fail("setVelocity must not be called");
        }

        @Override
        public double getPower() {
            return 0.25;
        }

        @Override
        public double getVelocity() {
            return 120.0;
        }

        @Override
        public double getCurrent(CurrentUnit unit) {
            if (!currentSupported) {
                throw new UnsupportedOperationException("current");
            }
            return 1.5;
        }

        @Override
        public int getCurrentPosition() {
            return 3;
        }

        @Override
        public String getDeviceName() {
            return "motor";
        }

        @Override
        public String getConnectionInfo() {
            return "";
        }
    }
}
