package org.allsparks.amper.battery;

import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.VoltageSample;

/**
 * Phase 0/1 battery observer. Later phases may estimate effective resistance;
 * those predictions must carry {@link EstimateConfidence} and remain disabled
 * by default.
 */
public final class BatteryEstimator {
    private double maxSeenVolts = Double.NaN;
    private double minSeenVolts = Double.NaN;

    public BatteryObservation update(ElectricalObservation observation) {
        VoltageSample filtered = observation.filteredVoltage();
        if (!filtered.isUsable()) {
            return new BatteryObservation(
                    Double.NaN,
                    maxSeenVolts,
                    minSeenVolts,
                    EstimateConfidence.none("voltage sensing invalid"));
        }

        double volts = filtered.volts();
        if (Double.isNaN(maxSeenVolts) || volts > maxSeenVolts) {
            maxSeenVolts = volts;
        }
        if (Double.isNaN(minSeenVolts) || volts < minSeenVolts) {
            minSeenVolts = volts;
        }

        double spread = maxSeenVolts - minSeenVolts;
        double score = spread < 0.25 ? 0.4 : Math.min(0.7, 0.4 + spread);
        return new BatteryObservation(
                volts,
                maxSeenVolts,
                minSeenVolts,
                new EstimateConfidence(score, "phase0 observation only; not a predictive model"));
    }

    public void reset() {
        maxSeenVolts = Double.NaN;
        minSeenVolts = Double.NaN;
    }
}
