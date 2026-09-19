package org.allsparks.amper.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.allsparks.contracts.input.InputDemand;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.MotorSignals;
import org.allsparks.contracts.input.Sample;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;
import org.junit.jupiter.api.Test;

class OvercurrentFollowUpTest {

    @Test
    void risingEdgeRequestsOnceAndCapsAtOneMotor() {
        Fixture values = new Fixture();
        values.flag("front_left_drive", true);
        values.flag("front_right_drive", true);
        OvercurrentFollowUp followUp =
                new OvercurrentFollowUp(values, values, Arrays.asList("front_left_drive", "front_right_drive"));
        followUp.afterSnapshot();
        assertEquals(1, values.requests);
        assertEquals(MotorSignals.currentAmps("front_left_drive"), values.lastRequested);
        values.current("front_left_drive", 2.0);
        followUp.afterSnapshot();
        assertEquals(2, values.requests);
        assertEquals(MotorSignals.currentAmps("front_right_drive"), values.lastRequested);
    }

    @Test
    void freshCurrentStopsFurtherRequestsUntilFallingEdge() {
        Fixture values = new Fixture();
        values.flag("front_left_drive", true);
        OvercurrentFollowUp followUp = new OvercurrentFollowUp(values, values, Arrays.asList("front_left_drive"));
        followUp.afterSnapshot();
        assertEquals(1, values.requests);
        values.current("front_left_drive", 4.2);
        followUp.afterSnapshot();
        assertEquals(1, values.requests);
        values.flag("front_left_drive", false);
        values.clearCurrent("front_left_drive");
        followUp.afterSnapshot();
        values.flag("front_left_drive", true);
        followUp.afterSnapshot();
        assertEquals(2, values.requests);
    }

    @Test
    void missingFlagDoesNotRequest() {
        Fixture values = new Fixture();
        OvercurrentFollowUp followUp = new OvercurrentFollowUp(values, values, Arrays.asList("front_left_drive"));
        followUp.afterSnapshot();
        assertEquals(0, values.requests);
    }

    @Test
    void alreadyQueuedKeyCountsAsThisCyclesCap() {
        Fixture values = new Fixture();
        values.flag("front_left_drive", true);
        values.flag("front_right_drive", true);
        values.queued.add(MotorSignals.currentAmps("front_left_drive"));
        OvercurrentFollowUp followUp =
                new OvercurrentFollowUp(values, values, Arrays.asList("front_left_drive", "front_right_drive"));
        followUp.afterSnapshot();
        assertEquals(0, values.requests);
        assertEquals(
                MotorSignals.currentAmps("front_left_drive"),
                values.queued.iterator().next());
    }

    private static final class Fixture implements InputValues, InputDemand {
        private final Map<SignalKey<?>, Sample<?>> samples = new HashMap<SignalKey<?>, Sample<?>>();
        final Set<SignalKey<?>> queued = new HashSet<SignalKey<?>>();
        int requests;
        SignalKey<?> lastRequested;

        void flag(String name, boolean over) {
            samples.put(
                    MotorSignals.overCurrent(name),
                    Sample.of(Boolean.valueOf(over), Validity.VALID, 1L, 1L, true, null));
        }

        void current(String name, double amps) {
            SignalKey<Double> key = MotorSignals.currentAmps(name);
            samples.put(key, Sample.of(Double.valueOf(amps), Validity.VALID, 2L, 1L, true, null));
            queued.remove(key);
        }

        void clearCurrent(String name) {
            samples.remove(MotorSignals.currentAmps(name));
        }

        @Override
        public void requestOnce(SignalKey<?> key) {
            requests++;
            lastRequested = key;
            queued.add(key);
        }

        @Override
        public boolean isRequested(SignalKey<?> key) {
            return queued.contains(key);
        }

        @Override
        public long currentCycleId() {
            return 1L;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Sample<T> get(SignalKey<T> key) {
            Sample<?> sample = samples.get(key);
            if (sample == null) {
                throw new IllegalArgumentException("unknown");
            }
            return (Sample<T>) sample;
        }

        @Override
        public boolean contains(SignalKey<?> key) {
            return samples.containsKey(key);
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
            Sample<?> sample = samples.get(key);
            if (sample == null || sample.orNull() == null) {
                return 0.0d;
            }
            return ((Double) sample.orNull()).doubleValue();
        }

        @Override
        public boolean getBoolean(SignalKey<Boolean> key) {
            Sample<?> sample = samples.get(key);
            return sample != null && Boolean.TRUE.equals(sample.orNull());
        }
    }

    @Test
    void motorCurrentKeysMatchContracts() {
        assertEquals(MotorSignals.overCurrent("x"), AmperSignals.overCurrent("x"));
        assertEquals(MotorSignals.currentAmps("x"), AmperSignals.motorCurrent("x"));
        assertFalse(AmperSignals.CONTROL_HUB_VOLTAGE.equals(AmperSignals.motorCurrent("x")));
    }
}
