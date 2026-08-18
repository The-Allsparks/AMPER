package org.allsparks.amper.telemetry;

import java.util.LinkedHashMap;
import java.util.Map;
import org.allsparks.amper.api.DriverPowerState;
import org.allsparks.amper.battery.BatteryObservation;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Classifies a concise driver-facing electrical state. Rate-limits publishes.
 * Never modifies motor output.
 */
public final class DriverFeedback {
    private DriverPowerState state = DriverPowerState.NORMAL;
    private DriverPowerState publishedState = DriverPowerState.NORMAL;
    private long lastPublishNanos = Long.MIN_VALUE / 4;
    private int elevatedCount;
    private int severeCount;
    private int invalidCount;
    private int weakBatteryCount;
    private int stallCount;

    public DriverTelemetry update(
            ElectricalObservation observation,
            BatteryObservation battery,
            boolean stallSuspected,
            PowerPolicy policy,
            PowerEventLogger logger) {
        DriverPowerState classified = classify(observation, battery, stallSuspected, policy);
        state = applyHysteresis(state, classified, observation, policy);

        boolean stateChanged = state != publishedState;
        boolean due = observation.loopStartNanos() - lastPublishNanos >= policy.telemetryMinPeriodNanos();
        boolean publish = stateChanged || due;
        if (publish) {
            lastPublishNanos = observation.loopStartNanos();
            if (stateChanged) {
                tally(state);
                if (logger != null && state != DriverPowerState.NORMAL) {
                    Map<String, String> fields = new LinkedHashMap<>();
                    fields.put("state", state.name());
                    logger.record(new PowerEvent(
                            observation.loopStartNanos(),
                            warningType(state),
                            state.name(),
                            fields));
                }
            }
            publishedState = state;
        }
        return new DriverTelemetry(state, publish, state.name());
    }

    public DriverPowerState state() {
        return state;
    }

    public int elevatedCount() {
        return elevatedCount;
    }

    public int severeCount() {
        return severeCount;
    }

    public int invalidCount() {
        return invalidCount;
    }

    public int weakBatteryCount() {
        return weakBatteryCount;
    }

    public int stallCount() {
        return stallCount;
    }

    public void reset() {
        state = DriverPowerState.NORMAL;
        publishedState = DriverPowerState.NORMAL;
        lastPublishNanos = Long.MIN_VALUE / 4;
        elevatedCount = 0;
        severeCount = 0;
        invalidCount = 0;
        weakBatteryCount = 0;
        stallCount = 0;
    }

    private void tally(DriverPowerState value) {
        switch (value) {
            case ELEVATED_DEMAND:
                elevatedCount++;
                break;
            case SEVERE_VOLTAGE_RISK:
                severeCount++;
                break;
            case INVALID_SENSING:
                invalidCount++;
                break;
            case SUSPECTED_WEAK_BATTERY:
                weakBatteryCount++;
                break;
            case SUSPECTED_STALLED_MECHANISM:
                stallCount++;
                break;
            default:
                break;
        }
    }

    private static PowerEventType warningType(DriverPowerState value) {
        if (value == DriverPowerState.INVALID_SENSING) {
            return PowerEventType.SENSOR_INVALID;
        }
        if (value == DriverPowerState.SUSPECTED_STALLED_MECHANISM) {
            return PowerEventType.STALL_SUSPECTED;
        }
        return PowerEventType.VOLTAGE_WARNING;
    }

    static DriverPowerState classify(
            ElectricalObservation observation,
            BatteryObservation battery,
            boolean stallSuspected,
            PowerPolicy policy) {
        if (!observation.sensingValid()) {
            return DriverPowerState.INVALID_SENSING;
        }
        double volts = observation.filteredVoltage().volts();
        if (!Double.isNaN(volts) && volts <= policy.criticalVoltageVolts()) {
            return DriverPowerState.SEVERE_VOLTAGE_RISK;
        }
        if (stallSuspected) {
            return DriverPowerState.SUSPECTED_STALLED_MECHANISM;
        }
        if (battery != null
                && !Double.isNaN(battery.restingHintVolts())
                && !Double.isNaN(battery.loadedHintVolts())
                && battery.restingHintVolts() - battery.loadedHintVolts() >= policy.weakBatterySagVolts()
                && battery.loadedHintVolts() <= policy.watchVoltageVolts()) {
            return DriverPowerState.SUSPECTED_WEAK_BATTERY;
        }
        if (!Double.isNaN(volts) && volts <= policy.watchVoltageVolts()) {
            return DriverPowerState.ELEVATED_DEMAND;
        }
        return DriverPowerState.NORMAL;
    }

    static DriverPowerState applyHysteresis(
            DriverPowerState current,
            DriverPowerState classified,
            ElectricalObservation observation,
            PowerPolicy policy) {
        if (classified == DriverPowerState.INVALID_SENSING
                || classified == DriverPowerState.SEVERE_VOLTAGE_RISK
                || classified == DriverPowerState.SUSPECTED_STALLED_MECHANISM) {
            return classified;
        }
        double volts = observation.filteredVoltage().volts();
        if (current == DriverPowerState.SEVERE_VOLTAGE_RISK
                && observation.sensingValid()
                && !Double.isNaN(volts)
                && volts < policy.recoveryVoltageVolts()) {
            return DriverPowerState.SEVERE_VOLTAGE_RISK;
        }
        if (current == DriverPowerState.ELEVATED_DEMAND
                && observation.sensingValid()
                && !Double.isNaN(volts)
                && volts < policy.recoveryVoltageVolts()
                && classified == DriverPowerState.NORMAL) {
            return DriverPowerState.ELEVATED_DEMAND;
        }
        return classified;
    }
}
