package org.allsparks.amper.telemetry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Detects mechanism start/stop from commanded effort. Does not command motors.
 */
public final class MechanismActivityTracker {
    private final Map<String, Boolean> active = new LinkedHashMap<>();
    private int starts;
    private int stops;

    public List<PowerEvent> update(ElectricalObservation observation, PowerPolicy policy, PowerEventLogger logger) {
        List<PowerEvent> emitted = new ArrayList<>();
        for (MotorSnapshot motor : observation.motors()) {
            double effort = motor.commandedEffort();
            if (Double.isNaN(effort)) {
                continue;
            }
            double abs = Math.abs(effort);
            boolean currently = Boolean.TRUE.equals(active.get(motor.motorId()));
            if (!currently && abs >= policy.mechanismStartEffort()) {
                active.put(motor.motorId(), true);
                starts++;
                PowerEvent event = transition(observation.loopStartNanos(), motor.motorId(), "start", abs);
                emitted.add(event);
                if (logger != null) {
                    logger.record(event);
                }
            } else if (currently && abs <= policy.mechanismStopEffort()) {
                active.put(motor.motorId(), false);
                stops++;
                PowerEvent event = transition(observation.loopStartNanos(), motor.motorId(), "stop", abs);
                emitted.add(event);
                if (logger != null) {
                    logger.record(event);
                }
            }
        }
        return emitted;
    }

    public int startCount() {
        return starts;
    }

    public int stopCount() {
        return stops;
    }

    public void reset() {
        active.clear();
        starts = 0;
        stops = 0;
    }

    private static PowerEvent transition(long timestampNanos, String motorId, String activity, double absEffort) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("motor", motorId);
        fields.put("activity", activity);
        fields.put("absEffort", Double.toString(absEffort));
        return new PowerEvent(timestampNanos, PowerEventType.STATE_TRANSITION, "mechanism_" + activity, fields);
    }
}
