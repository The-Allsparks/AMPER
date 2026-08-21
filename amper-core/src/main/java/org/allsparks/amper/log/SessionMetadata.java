package org.allsparks.amper.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.allsparks.amper.AmperVersion;

/** Immutable session header written into AMPER CSV exports. */
public final class SessionMetadata {
    private final String sessionId;
    private final String schemaVersion;
    private final String amperVersion;
    private final String robotNote;
    private final String policyNote;
    private final Map<String, String> extra;

    public SessionMetadata(String sessionId, String robotNote, String policyNote, Map<String, String> extra) {
        this.sessionId = sessionId == null || sessionId.trim().isEmpty() ? "unspecified" : sessionId.trim();
        this.schemaVersion = AmperVersion.CSV_SCHEMA_VERSION;
        this.amperVersion = AmperVersion.VERSION;
        this.robotNote = robotNote == null ? "" : robotNote;
        this.policyNote = policyNote == null ? "" : policyNote;
        this.extra = Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(extra == null ? Collections.<String, String>emptyMap() : extra));
    }

    public static SessionMetadata anonymous(String policyNote) {
        return new SessionMetadata("amper-session", "", policyNote, Collections.<String, String>emptyMap());
    }

    public String sessionId() {
        return sessionId;
    }

    public String schemaVersion() {
        return schemaVersion;
    }

    public String amperVersion() {
        return amperVersion;
    }

    public String robotNote() {
        return robotNote;
    }

    public String policyNote() {
        return policyNote;
    }

    public Map<String, String> extra() {
        return extra;
    }

    /**
     * Hardware family for {@code /AMPER/Metadata/HardwarePlatform}.
     * Defaults to {@code FTC_REV_HUB}. Never claims SystemCore unless callers set it.
     */
    public String hardwarePlatform() {
        String value = extra.get("hardwarePlatform");
        if (value == null || value.trim().isEmpty()) {
            return "FTC_REV_HUB";
        }
        return value.trim();
    }
}
