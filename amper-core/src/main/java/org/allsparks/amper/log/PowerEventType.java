package org.allsparks.amper.log;

/** Categories of logged electrical events. */
public enum PowerEventType {
    LOOP_SAMPLE,
    VOLTAGE_WARNING,
    CURRENT_WARNING,
    SENSOR_INVALID,
    STATE_TRANSITION,
    INTERVENTION,
    STALL_SUSPECTED,
    MATCH_SUMMARY,
    DUPLICATE_OBSERVE,
    LIFECYCLE,
    EXPORT
}
