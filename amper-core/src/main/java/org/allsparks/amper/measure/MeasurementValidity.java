package org.allsparks.amper.measure;

/** Validity classification for a single electrical measurement. */
public enum MeasurementValidity {
    /** Fresh, in-range, and usable. */
    VALID,
    /** Previously captured value whose age exceeds the stale threshold. */
    STALE,
    /** Hardware read failed or returned NaN. Never treat as zero. */
    MISSING,
    /** Numeric value outside the configured valid range. */
    OUT_OF_RANGE,
    /** This quantity is not available from the hardware/API. */
    UNSUPPORTED,
    /**
     * Not sampled on this loop (for example round-robin current). The numeric
     * payload, if any, is from an earlier capture and must not be treated as
     * fresh.
     */
    SKIPPED
}
