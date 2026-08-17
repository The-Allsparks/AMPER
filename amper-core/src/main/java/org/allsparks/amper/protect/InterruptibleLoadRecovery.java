package org.allsparks.amper.protect;

/**
 * Representation of an interruptible-load recovery policy. Automatic recovery
 * is not performed by default.
 */
public final class InterruptibleLoadRecovery {
    private final long dwellNanos;
    private final boolean automaticRecoveryEnabled;

    public InterruptibleLoadRecovery(long dwellNanos, boolean automaticRecoveryEnabled) {
        if (dwellNanos < 0L) {
            throw new IllegalArgumentException("dwellNanos must be nonnegative");
        }
        this.dwellNanos = dwellNanos;
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    public static InterruptibleLoadRecovery disabled() {
        return new InterruptibleLoadRecovery(0L, false);
    }

    public long dwellNanos() {
        return dwellNanos;
    }

    public boolean automaticRecoveryEnabled() {
        return automaticRecoveryEnabled;
    }
}
