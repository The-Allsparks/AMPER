package org.allsparks.amper.battery;

/**
 * Confidence attached to a battery estimate. Never treat estimates as certain.
 */
public final class EstimateConfidence {
    private final double score;
    private final String note;

    public EstimateConfidence(double score, String note) {
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("score must be in [0, 1]");
        }
        this.score = score;
        this.note = note == null ? "" : note;
    }

    public static EstimateConfidence none(String note) {
        return new EstimateConfidence(0.0, note);
    }

    public double score() {
        return score;
    }

    public String note() {
        return note;
    }

    public boolean isActionable(double minimumScore) {
        return score >= minimumScore;
    }
}
