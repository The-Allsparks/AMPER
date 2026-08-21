package org.allsparks.amper.battery;

/** Basic battery-condition observation for Phase 0/1. */
public final class BatteryObservation {
    private final double latestVolts;
    private final double restingHintVolts;
    private final double loadedHintVolts;
    private final EstimateConfidence confidence;

    public BatteryObservation(
            double latestVolts, double restingHintVolts, double loadedHintVolts, EstimateConfidence confidence) {
        this.latestVolts = latestVolts;
        this.restingHintVolts = restingHintVolts;
        this.loadedHintVolts = loadedHintVolts;
        this.confidence = confidence == null ? EstimateConfidence.none("unspecified") : confidence;
    }

    public double latestVolts() {
        return latestVolts;
    }

    public double restingHintVolts() {
        return restingHintVolts;
    }

    public double loadedHintVolts() {
        return loadedHintVolts;
    }

    public EstimateConfidence confidence() {
        return confidence;
    }
}
