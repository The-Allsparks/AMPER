package org.allsparks.amper.policy;

/**
 * Where a numeric threshold came from. Placeholder values are not FTC truth
 * and must not be labeled hardware-validated.
 */
public enum ThresholdProvenance {
    /** Derived from measurements on a specific robot (team notes required). */
    MEASURED_DEFAULT,
    /** Conservative starting point; not claimed to match every battery or hub. */
    CONSERVATIVE_PLACEHOLDER,
    /** Explicitly chosen by the team for their robot. */
    TEAM_TUNED,
    /** Confirmed by documented Control Hub characterization. */
    HARDWARE_VALIDATED
}
