package org.allsparks.amper.battery;

import java.util.ArrayDeque;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.VoltageSample;

/**
 * Phase 0/1 battery observer. Resting and loaded hints are a recent voltage
 * window, not match-long extrema. Later phases may estimate effective
 * resistance; those predictions must carry {@link EstimateConfidence} and
 * remain disabled by default.
 */
public final class BatteryEstimator {
    /** Recent-window length for rest vs load hints. Not a predictive model. */
    public static final long HINT_WINDOW_NANOS = 2_000_000_000L;

    private final ArrayDeque<WindowSample> window = new ArrayDeque<WindowSample>();

    public BatteryObservation update(ElectricalObservation observation) {
        VoltageSample filtered = observation.filteredVoltage();
        prune(observation.loopStartNanos());
        if (!filtered.isUsable()) {
            double[] hints = windowMinMax();
            return new BatteryObservation(
                    Double.NaN,
                    hints[0],
                    hints[1],
                    EstimateConfidence.none("voltage sensing invalid"));
        }

        double volts = filtered.volts();
        window.addLast(new WindowSample(observation.loopStartNanos(), volts));
        prune(observation.loopStartNanos());
        double[] hints = windowMinMax();
        double rest = hints[0];
        double loaded = hints[1];
        double spread = (!Double.isNaN(rest) && !Double.isNaN(loaded)) ? rest - loaded : 0.0;
        double score = spread < 0.25 ? 0.4 : Math.min(0.7, 0.4 + spread);
        return new BatteryObservation(
                volts,
                rest,
                loaded,
                new EstimateConfidence(
                        score,
                        "recent " + (HINT_WINDOW_NANOS / 1_000_000_000L)
                                + "s window; not a predictive model"));
    }

    public void reset() {
        window.clear();
    }

    private void prune(long nowNanos) {
        while (!window.isEmpty() && nowNanos - window.peekFirst().nanos > HINT_WINDOW_NANOS) {
            window.removeFirst();
        }
    }

    /** {@code [restingHint, loadedHint]} from the current window. */
    private double[] windowMinMax() {
        double max = Double.NaN;
        double min = Double.NaN;
        for (WindowSample sample : window) {
            if (Double.isNaN(max) || sample.volts > max) {
                max = sample.volts;
            }
            if (Double.isNaN(min) || sample.volts < min) {
                min = sample.volts;
            }
        }
        return new double[] {max, min};
    }

    private static final class WindowSample {
        private final long nanos;
        private final double volts;

        private WindowSample(long nanos, double volts) {
            this.nanos = nanos;
            this.volts = volts;
        }
    }
}
