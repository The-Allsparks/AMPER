package org.allsparks.amper.telemetry;

import org.allsparks.amper.api.DriverPowerState;

/** Rate-limited driver-facing snapshot. Contains no motor commands. */
public final class DriverTelemetry {
    private final DriverPowerState state;
    private final boolean publishedThisCycle;
    private final String message;

    public DriverTelemetry(DriverPowerState state, boolean publishedThisCycle, String message) {
        this.state = state == null ? DriverPowerState.NORMAL : state;
        this.publishedThisCycle = publishedThisCycle;
        this.message = message == null ? "" : message;
    }

    public DriverPowerState state() {
        return state;
    }

    public boolean publishedThisCycle() {
        return publishedThisCycle;
    }

    public String message() {
        return message;
    }
}
