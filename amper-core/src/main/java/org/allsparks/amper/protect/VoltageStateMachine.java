package org.allsparks.amper.protect;

import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Hysteresis + dwell voltage state machine. Pure software foundation for
 * Phase 3. Does not command motors.
 *
 * <p><strong>Not student API.</strong> Experimental, default-off, and not
 * competition-ready. Intervention remains off unless the Phase 3 feature
 * flag is set, and even then this class only reports state.
 */
public final class VoltageStateMachine {
    private VoltageProtectionState state = VoltageProtectionState.NORMAL;
    private long dwellStartedNanos = Long.MIN_VALUE / 4;

    public VoltageProtectionState update(ElectricalObservation observation, PowerPolicy policy) {
        if (observation == null || !observation.sensingValid() || observation.disabled()) {
            state = VoltageProtectionState.SENSOR_FAULT;
            dwellStartedNanos = observation == null ? 0L : observation.loopStartNanos();
            return state;
        }
        double volts = observation.filteredVoltage().volts();
        if (Double.isNaN(volts) || !observation.filteredVoltage().isUsable()) {
            state = VoltageProtectionState.SENSOR_FAULT;
            dwellStartedNanos = observation.loopStartNanos();
            return state;
        }

        VoltageProtectionState classified;
        if (volts <= policy.criticalVoltageVolts()) {
            classified = VoltageProtectionState.CRITICAL;
        } else if (volts <= policy.limitingVoltageVolts()) {
            classified = VoltageProtectionState.LIMITING;
        } else if (volts <= policy.watchVoltageVolts()) {
            classified = VoltageProtectionState.WATCH;
        } else {
            classified = VoltageProtectionState.NORMAL;
        }

        if (state == VoltageProtectionState.SENSOR_FAULT) {
            if (classified == VoltageProtectionState.NORMAL
                    && observation.loopStartNanos() - dwellStartedNanos >= policy.recoveryHoldNanos()) {
                state = VoltageProtectionState.NORMAL;
            }
            return state;
        }

        if (severity(classified) > severity(state)) {
            state = classified;
            dwellStartedNanos = observation.loopStartNanos();
            return state;
        }

        if (state == VoltageProtectionState.CRITICAL
                || state == VoltageProtectionState.LIMITING
                || state == VoltageProtectionState.WATCH) {
            if (volts >= policy.recoveryVoltageVolts()) {
                if (observation.loopStartNanos() - dwellStartedNanos >= policy.recoveryHoldNanos()) {
                    state = VoltageProtectionState.RECOVERY;
                    dwellStartedNanos = observation.loopStartNanos();
                }
            } else {
                dwellStartedNanos = observation.loopStartNanos();
            }
            return state;
        }

        if (state == VoltageProtectionState.RECOVERY) {
            if (volts >= policy.recoveryVoltageVolts()
                    && observation.loopStartNanos() - dwellStartedNanos >= policy.recoveryHoldNanos()) {
                state = VoltageProtectionState.NORMAL;
            } else if (severity(classified) >= severity(VoltageProtectionState.LIMITING)) {
                state = classified;
                dwellStartedNanos = observation.loopStartNanos();
            }
            return state;
        }

        state = classified;
        return state;
    }

    public VoltageProtectionState state() {
        return state;
    }

    /** Intervention must be off in SENSOR_FAULT regardless of feature flags. */
    public boolean interventionPermitted(PowerPolicy policy) {
        if (state == VoltageProtectionState.SENSOR_FAULT) {
            return false;
        }
        return policy.featureFlags().isPhase3ReactiveVoltage();
    }

    public void reset() {
        state = VoltageProtectionState.NORMAL;
        dwellStartedNanos = Long.MIN_VALUE / 4;
    }

    private static int severity(VoltageProtectionState value) {
        switch (value) {
            case SENSOR_FAULT:
                return 5;
            case CRITICAL:
                return 4;
            case LIMITING:
                return 3;
            case WATCH:
                return 2;
            case RECOVERY:
                return 1;
            default:
                return 0;
        }
    }
}
