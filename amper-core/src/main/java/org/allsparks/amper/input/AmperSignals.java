package org.allsparks.amper.input;

import org.allsparks.contracts.input.MotorSignals;
import org.allsparks.contracts.input.SignalKey;

/**
 * Electrical {@link SignalKey} identities AMPER declares on an
 * {@link org.allsparks.contracts.input.InputRegistrar}.
 *
 * <p>These keys are the contracts SPI, not PULSE types. A sampler such as PULSE
 * may implement the registrar. AMPER still compiles and runs without PULSE:
 * {@code AmperFtc} keeps the raw {@code VoltageSensor} path when no registrar
 * is supplied.
 *
 * <p>AMPER's own {@code org.allsparks.amper.policy.SamplingPolicy} (current-read
 * budget) is a different type from the contracts capture cadence used with
 * these keys.
 */
public final class AmperSignals {
    private AmperSignals() {}

    /**
     * Control Hub battery bus. Stable identity so TeamCode can bind
     * {@code VoltageSensor#getVoltage()} once.
     */
    public static final SignalKey<Double> CONTROL_HUB_VOLTAGE = SignalKey.doubleKey("amper", "controlHubVoltage");

    /**
     * Named hub or labeled bus voltage (Expansion Hub, extra sensor, cached
     * supplier). {@code hubName} is the HardwareMap / display label, not a
     * PULSE type.
     */
    public static SignalKey<Double> hubVoltage(String hubName) {
        if (hubName == null) {
            throw new IllegalArgumentException("hubName is required");
        }
        String trimmed = hubName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("hubName must not be blank");
        }
        return SignalKey.doubleKey("amper", "hubVoltage." + trimmed);
    }

    /**
     * Bulk over-current flag for a named motor. Same identity as
     * {@link MotorSignals#overCurrent(String)}.
     */
    public static SignalKey<Boolean> overCurrent(String deviceName) {
        return MotorSignals.overCurrent(deviceName);
    }

    /**
     * On-demand motor current in amperes. Same identity as
     * {@link MotorSignals#currentAmps(String)}.
     */
    public static SignalKey<Double> motorCurrent(String deviceName) {
        return MotorSignals.currentAmps(deviceName);
    }
}
