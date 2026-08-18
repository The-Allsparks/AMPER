package org.allsparks.amper.telemetry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Observes possible stalls from command + low velocity + high current.
 * Warning only; never changes motor output.
 */
public final class StallSuspicionTracker {
    private final Map<String, Long> dwellStartedNanos = new LinkedHashMap<String, Long>();
    private final Set<String> suspectedIds = new LinkedHashSet<String>();
    private boolean suspected;

    public boolean update(ElectricalObservation observation, PowerPolicy policy, PowerEventLogger logger) {
        suspected = false;
        suspectedIds.clear();
        for (MotorSnapshot motor : observation.motors()) {
            if (looksStalled(motor, policy)) {
                Long started = dwellStartedNanos.get(motor.motorId());
                if (started == null) {
                    dwellStartedNanos.put(motor.motorId(), observation.loopStartNanos());
                    continue;
                }
                if (observation.loopStartNanos() - started >= policy.stallDwellNanos()) {
                    suspected = true;
                    suspectedIds.add(motor.motorId());
                    if (logger != null) {
                        Map<String, String> fields = new LinkedHashMap<String, String>();
                        fields.put("motor", motor.motorId());
                        fields.put("amps", Double.toString(motor.current().amps()));
                        fields.put("cmd", Double.toString(motor.commandedEffort()));
                        fields.put("vel", Double.toString(motor.velocityTicksPerSecond()));
                        logger.record(new PowerEvent(
                                observation.loopStartNanos(),
                                PowerEventType.STALL_SUSPECTED,
                                "stall_suspected",
                                fields));
                    }
                }
            } else {
                dwellStartedNanos.remove(motor.motorId());
            }
        }
        return suspected;
    }

    public boolean suspected() {
        return suspected;
    }

    public Set<String> suspectedMotorIds() {
        return Collections.unmodifiableSet(suspectedIds);
    }

    public void reset() {
        dwellStartedNanos.clear();
        suspectedIds.clear();
        suspected = false;
    }

    private static boolean looksStalled(MotorSnapshot motor, PowerPolicy policy) {
        double cmd = motor.commandedEffort();
        double vel = motor.velocityTicksPerSecond();
        if (Double.isNaN(cmd) || Math.abs(cmd) < policy.mechanismStartEffort()) {
            return false;
        }
        if (Double.isNaN(vel)) {
            return false;
        }
        if (Math.abs(vel) > policy.stallVelocityTicksPerSecond()) {
            return false;
        }
        if (!motor.current().isUsable()) {
            return false;
        }
        return motor.current().amps() >= policy.stallCurrentAmps();
    }
}
