package org.allsparks.amper.protect;

import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;

/**
 * Phase 3 voltage-state foundation. Output intervention stays disabled unless
 * Phase 3 is explicitly enabled. {@link #SENSOR_FAULT} must disable
 * intervention. Not robot-ready without hardware evidence.
 */
public enum VoltageProtectionState {
    NORMAL,
    WATCH,
    LIMITING,
    CRITICAL,
    RECOVERY,
    SENSOR_FAULT
}
