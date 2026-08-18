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
        assertThrows(IllegalArgumentException.class, () -> AmperFtc.builder(map)
                .controlHubVoltage()
                .expansionHubVoltage("Expansion Hub 9"));
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
                .policy(AmperPolicies.passiveDefaults())
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
                .persistLogs(false)
                .build();
        ElectricalObservation obs = session.observe();
        assertEquals(MeasurementValidity.UNSUPPORTED, obs.motors().get(0).current().validity());
        assertTrue(Double.isNaN(obs.motors().get(0).current().amps()));
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
