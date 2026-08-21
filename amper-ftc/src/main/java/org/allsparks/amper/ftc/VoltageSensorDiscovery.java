package org.allsparks.amper.ftc;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Deterministic voltage-sensor discovery. Never silently picks
 * {@code iterator().next()}.
 */
public final class VoltageSensorDiscovery {
    private VoltageSensorDiscovery() {}

    public static List<String> names(HardwareMap hardwareMap) {
        List<String> names = new ArrayList<String>();
        if (hardwareMap == null || hardwareMap.voltageSensor == null) {
            return names;
        }
        for (Map.Entry<String, VoltageSensor> entry : hardwareMap.voltageSensor.entrySet()) {
            names.add(entry.getKey());
        }
        return names;
    }

    public static VoltageSensor requireNamed(HardwareMap hardwareMap, String deviceName) {
        if (hardwareMap == null) {
            throw new IllegalArgumentException("hardwareMap is required");
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("voltage sensor name is required");
        }
        try {
            return hardwareMap.voltageSensor.get(deviceName);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "No voltage sensor named '" + deviceName + "'. Available: " + names(hardwareMap), ex);
        }
    }

    /**
     * Finds the unique sensor whose configured name contains {@code needle}
     * (case-insensitive). Throws if zero or multiple match.
     */
    public static NamedSensor requireUniqueContaining(HardwareMap hardwareMap, String needle) {
        if (needle == null || needle.trim().isEmpty()) {
            throw new IllegalArgumentException("voltage sensor name pattern is required");
        }
        String want = needle.toLowerCase(Locale.US);
        List<NamedSensor> matches = new ArrayList<NamedSensor>();
        for (Map.Entry<String, VoltageSensor> entry : hardwareMap.voltageSensor.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.US).contains(want)) {
                matches.add(new NamedSensor(entry.getKey(), entry.getValue()));
            }
        }
        if (matches.size() != 1) {
            throw new IllegalArgumentException("Expected exactly one voltage sensor containing '" + needle + "', found "
                    + matches.size() + ". Available: " + names(hardwareMap)
                    + ". Pass an explicit device name instead of guessing.");
        }
        return matches.get(0);
    }

    public static final class NamedSensor {
        public final String name;
        public final VoltageSensor sensor;

        public NamedSensor(String name, VoltageSensor sensor) {
            this.name = name;
            this.sensor = sensor;
        }
    }
}
