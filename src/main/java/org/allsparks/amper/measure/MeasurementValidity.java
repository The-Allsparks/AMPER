package org.allsparks.amper.measure;

/** Validity classification for a single electrical measurement. */
public enum MeasurementValidity {
    VALID,
    STALE,
    MISSING,
    OUT_OF_RANGE,
    UNSUPPORTED
}
