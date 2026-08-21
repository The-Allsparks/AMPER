package org.allsparks.amper.predict;

import org.allsparks.amper.measure.ElectricalObservation;

/**
 * Phase 5 shadow recorder. Logs requested demand vs subsequent voltage for
 * offline study. Never used for actuation. Not a machine-learned model.
 *
 * <p><strong>Not student API.</strong> Experimental, unused on the observe
 * path, and not competition-ready.
 */
public final class ShadowSagPredictor {
    private double lastDemand;
    private double lastVoltage;
    private boolean hasSample;

    public void record(ElectricalObservation observation, double requestedAbsDemand) {
        if (observation == null || !observation.sensingValid()) {
            return;
        }
        lastDemand = requestedAbsDemand;
        lastVoltage = observation.filteredVoltage().volts();
        hasSample = true;
    }

    public boolean hasSample() {
        return hasSample;
    }

    public double lastDemand() {
        return lastDemand;
    }

    public double lastVoltage() {
        return lastVoltage;
    }

    public void reset() {
        hasSample = false;
        lastDemand = Double.NaN;
        lastVoltage = Double.NaN;
    }
}
