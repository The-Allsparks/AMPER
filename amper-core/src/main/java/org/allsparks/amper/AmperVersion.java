package org.allsparks.amper;

/**
 * Reproducible version metadata for logs and artifacts.
 *
 * <p>Keep this string aligned with the Gradle {@code version} property.
 */
public final class AmperVersion {
    /** Library version. Hardware validation is not implied by this number. */
    public static final String VERSION = "0.1.0-rc.1";

    /** Internal event-CSV schema identifier (diagnostic, not AdvantageScope). */
    public static final String CSV_SCHEMA_VERSION = "1";

    /** Canonical /AMPER field catalog and AdvantageScope sidecar schema. */
    public static final String LOG_SCHEMA_VERSION = "1";

    private AmperVersion() {}
}
