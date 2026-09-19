package org.allsparks.amper.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.allsparks.contracts.input.InputDemand;
import org.allsparks.contracts.input.InputValues;
import org.allsparks.contracts.input.MotorSignals;
import org.allsparks.contracts.input.SignalKey;
import org.allsparks.contracts.observation.Validity;

/**
 * Watches bulk over-current flags and demands one motor-current capture on
 * the next sampling cycle. Cap is one {@link InputDemand#requestOnce} per
 * {@link #afterSnapshot()}, including a key already {@link InputDemand#isRequested}.
 *
 * <p>Does not read hardware. The sampler that implements {@link InputDemand}
 * captures {@code getCurrent} on the following cycle.
 */
public final class OvercurrentFollowUp {
    private final InputValues values;
    private final InputDemand demand;
    private final String[] names;
    private final SignalKey<Boolean>[] flagKeys;
    private final SignalKey<Double>[] currentKeys;
    private final boolean[] gotSample;

    @SuppressWarnings("unchecked")
    public OvercurrentFollowUp(InputValues values, InputDemand demand, List<String> deviceNames) {
        this.values = Objects.requireNonNull(values, "values");
        this.demand = Objects.requireNonNull(demand, "demand");
        List<String> copy = new ArrayList<String>(Objects.requireNonNull(deviceNames, "deviceNames"));
        this.names = new String[copy.size()];
        this.flagKeys = new SignalKey[copy.size()];
        this.currentKeys = new SignalKey[copy.size()];
        for (int i = 0; i < copy.size(); i++) {
            String name = copy.get(i);
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("deviceName must not be blank");
            }
            this.names[i] = name.trim();
            this.flagKeys[i] = MotorSignals.overCurrent(this.names[i]);
            this.currentKeys[i] = MotorSignals.currentAmps(this.names[i]);
        }
        this.gotSample = new boolean[this.names.length];
    }

    public List<String> deviceNames() {
        return Collections.unmodifiableList(Arrays.asList(names));
    }

    /**
     * Peek this cycle's flags and currents, then queue at most one follow-up
     * current read for the next capture.
     */
    public void afterSnapshot() {
        int requested = 0;
        for (int i = 0; i < names.length; i++) {
            boolean over = values.tryGetBoolean(flagKeys[i]) && values.isFresh(flagKeys[i]);
            double amps = values.tryGetDouble(currentKeys[i]);
            if (!Double.isNaN(amps)
                    && values.isFresh(currentKeys[i])
                    && values.validity(currentKeys[i]) == Validity.VALID) {
                gotSample[i] = true;
            }
            if (!over) {
                gotSample[i] = false;
                continue;
            }
            if (gotSample[i] || requested > 0) {
                continue;
            }
            if (demand.isRequested(currentKeys[i])) {
                requested++;
                continue;
            }
            demand.requestOnce(currentKeys[i]);
            requested++;
        }
    }

    public void reset() {
        Arrays.fill(gotSample, false);
    }
}
