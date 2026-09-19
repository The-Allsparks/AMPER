package org.allsparks.amper.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.VoltageSample;
import org.allsparks.contracts.input.InputPriority;
import org.allsparks.contracts.input.InputRegistrar;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.Sample;
import org.allsparks.contracts.input.SamplingPolicy;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;
import org.junit.jupiter.api.Test;

class InputValuesPowerSourceTest {
    private static final SignalKey<Double> KEY = AmperSignals.CONTROL_HUB_VOLTAGE;

    @Test
    void validSampleDoesNotInventAZero() {
        Fixture values = new Fixture(12.4, Validity.VALID, 50L, true);
        InputValuesPowerSource source = new InputValuesPowerSource(values, KEY, "Control Hub");
        VoltageSample sample = source.readBusVoltage(99L);
        assertEquals(12.4, sample.volts(), 1e-9);
        assertEquals(MeasurementValidity.VALID, sample.validity());
        assertEquals(50L, sample.capturedAtNanos());
        assertEquals("Control Hub", sample.sourceId());
    }

    @Test
    void staleKeepsTheCarriedVolts() {
        Fixture values = new Fixture(11.1, Validity.STALE, 10L, false);
        InputValuesPowerSource source = new InputValuesPowerSource(values, KEY, "bus");
        VoltageSample sample = source.readBusVoltage(1000L);
        assertEquals(11.1, sample.volts(), 1e-9);
        assertEquals(MeasurementValidity.STALE, sample.validity());
        assertEquals(10L, sample.capturedAtNanos());
    }

    @Test
    void invalidAndNanBecomeMissingNotZero() {
        Fixture values = new Fixture(Double.NaN, Validity.INVALID, 0L, false);
        InputValuesPowerSource source = new InputValuesPowerSource(values, KEY, "bus");
        VoltageSample sample = source.readBusVoltage(7L);
        assertTrue(Double.isNaN(sample.volts()));
        assertEquals(MeasurementValidity.MISSING, sample.validity());
    }

    @Test
    void batteryCurrentStaysUnsupported() {
        Fixture values = new Fixture(12.0, Validity.VALID, 1L, true);
        InputValuesPowerSource source = new InputValuesPowerSource(values, KEY, "hub");
        assertEquals(
                MeasurementValidity.UNSUPPORTED, source.readBatteryCurrent(1L).validity());
    }

    @Test
    void mapValidityCoversContractsEnum() {
        assertEquals(MeasurementValidity.VALID, InputValuesPowerSource.mapValidity(Validity.VALID));
        assertEquals(MeasurementValidity.STALE, InputValuesPowerSource.mapValidity(Validity.STALE));
        assertEquals(MeasurementValidity.MISSING, InputValuesPowerSource.mapValidity(Validity.MISSING));
        assertEquals(MeasurementValidity.MISSING, InputValuesPowerSource.mapValidity(Validity.INVALID));
        assertEquals(MeasurementValidity.OUT_OF_RANGE, InputValuesPowerSource.mapValidity(Validity.OUT_OF_RANGE));
        assertEquals(MeasurementValidity.UNSUPPORTED, InputValuesPowerSource.mapValidity(Validity.UNSUPPORTED));
    }

    private static final class Fixture implements InputValues, InputRegistrar {
        private final double volts;
        private final Validity validity;
        private final long captureNanos;
        private final boolean updated;

        Fixture(double volts, Validity validity, long captureNanos, boolean updated) {
            this.volts = volts;
            this.validity = validity;
            this.captureNanos = captureNanos;
            this.updated = updated;
        }

        @Override
        public void require(SignalKey<?> key, SamplingPolicy policy, InputPriority priority) {}

        @Override
        public void requireGroup(
                String groupId, SamplingPolicy policy, InputPriority priority, SignalKey<?>... members) {}

        @Override
        public long currentCycleId() {
            return 1L;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Sample<T> get(SignalKey<T> key) {
            if (validity == Validity.MISSING) {
                return Sample.missing();
            }
            Double boxed = Double.isNaN(volts) ? null : Double.valueOf(volts);
            return (Sample<T>) Sample.of(boxed, validity, captureNanos, 1L, updated, null);
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
            return validity;
        }

        @Override
        public long captureTimestampNanos(SignalKey<?> key) {
            return captureNanos;
        }

        @Override
        public boolean getBoolean(SignalKey<Boolean> key) {
            return false;
        }
    }
}
