package com.qualcomm.robotcore.hardware;

import android.content.Context;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Compile-only {@code HardwareMap} with enough surface for AMPER discovery.
 * Robot builds use the official FTC SDK class of the same name.
 */
public class HardwareMap {
    public Context appContext = new Context();
    public final DeviceMapping<VoltageSensor> voltageSensor = new DeviceMapping<VoltageSensor>();
    public final DeviceMapping<DcMotor> dcMotor = new DeviceMapping<DcMotor>();
    private final Map<String, Object> devices = new LinkedHashMap<String, Object>();

    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        Object found = devices.get(deviceName);
        if (found == null) {
            throw new IllegalArgumentException("Unable to find a hardware device with the name " + deviceName);
        }
        return classOrInterface.cast(found);
    }

    public void put(String name, Object device) {
        devices.put(name, device);
        if (device instanceof VoltageSensor) {
            voltageSensor.put(name, (VoltageSensor) device);
        }
        if (device instanceof DcMotor) {
            dcMotor.put(name, (DcMotor) device);
        }
    }

    public static class DeviceMapping<DEVICE_TYPE extends HardwareDevice> implements Iterable<DEVICE_TYPE> {
        private final LinkedHashMap<String, DEVICE_TYPE> map = new LinkedHashMap<String, DEVICE_TYPE>();

        public DEVICE_TYPE get(String deviceName) {
            DEVICE_TYPE device = map.get(deviceName);
            if (device == null) {
                throw new IllegalArgumentException("Unable to find a hardware device with the name " + deviceName);
            }
            return device;
        }

        public void put(String name, DEVICE_TYPE device) {
            map.put(name, device);
        }

        public Set<Map.Entry<String, DEVICE_TYPE>> entrySet() {
            return map.entrySet();
        }

        public int size() {
            return map.size();
        }

        @Override
        public Iterator<DEVICE_TYPE> iterator() {
            return map.values().iterator();
        }
    }
}
