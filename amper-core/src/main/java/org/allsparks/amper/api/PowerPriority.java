package org.allsparks.amper.api;

/**
 * Relative priority for a power request. Exact ordering is policy-defined and
 * may depend on operating state, not only subsystem identity.
 */
public enum PowerPriority {
    SURVIVAL,
    SAFETY_HOLD,
    MOBILITY_MINIMUM,
    SCORING_CRITICAL,
    DRIVETRAIN_NORMAL,
    AUXILIARY
}
