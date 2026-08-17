package org.allsparks.amper;

/** Per-OpMode session lifecycle. AMPER does not require process-global lifetime. */
public enum AmperLifecycle {
    CONSTRUCTED,
    INITIALIZED,
    STARTED,
    STOPPED,
    CLOSED
}
