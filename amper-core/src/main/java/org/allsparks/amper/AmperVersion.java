package org.allsparks.amper;

/**
 * Reproducible version metadata for logs and artifacts.
 *
 * <p>Keep this string aligned with the Gradle {@code version} property.
 */
public final class AmperVersion {
    /** Library version. Hardware validation is not implied by this number. */
    public static final String VERSION = "0.1.0-rc.1";

    /** Stable AMPER CSV schema identifier. */
    public static final String CSV_SCHEMA_VERSION = "1";

    private AmperVersion() {
    }
}
