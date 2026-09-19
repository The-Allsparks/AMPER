package org.allsparks.amper.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.MotorSignals;
import org.allsparks.contracts.input.Sample;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;
import org.junit.jupiter.api.Test;

class InputValuesMotorTelemetryTest {
    private static final SignalKey<Double> KEY = MotorSignals.currentAmps("front_left_drive");

    @Test
    void freshCurrentIsRecorded() {
        Fixture values = new Fixture(3.5, true);
        InputValuesMotorTelemetry motor = new InputValuesMotorTelemetry("front_left_drive", values, KEY, 100_000_000L);
        CurrentSample sample = motor.readCurrent(50L);
        assertEquals(3.5, sample.amps(), 1e-9);
        assertEquals(MeasurementValidity.VALID, sample.validity());
        assertTrue(motor.currentIsCachePeek());
    }

    @Test
    void missingCurrentIsSkippedNotZero() {
        Fixture values = new Fixture(Double.NaN, false);
        InputValuesMotorTelemetry motor = new InputValuesMotorTelemetry("front_left_drive", values, KEY, 100_000_000L);
        CurrentSample sample = motor.readCurrent(50L);
        assertTrue(Double.isNaN(sample.amps()));
        assertEquals(MeasurementValidity.SKIPPED, sample.validity());
    }

    private static final class Fixture implements InputValues {
        private final double amps;
        private final boolean fresh;

        Fixture(double amps, boolean fresh) {
            this.amps = amps;
            this.fresh = fresh;
        }

        @Override
        public long currentCycleId() {
            return 1L;
        }

        @Override
        public boolean contains(SignalKey<?> key) {
            return fresh;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Sample<T> get(SignalKey<T> key) {
            if (!fresh) {
                return Sample.missing();
            }
            return (Sample<T>) Sample.of(Double.valueOf(amps), Validity.VALID, 10L, 1L, true, null);
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
            return amps;
        }

        @Override
        public boolean getBoolean(SignalKey<Boolean> key) {
            return false;
        }
    }
}
