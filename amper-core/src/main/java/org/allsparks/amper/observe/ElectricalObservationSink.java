package org.allsparks.amper.observe;

import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.telemetry.DriverTelemetry;

/**
 * Optional observer of AMPER samples and lifecycle. TRACE, tests, or DS glue
 * implement this. AMPER does not import TRACE. Default is {@link #NOOP}.
 *
 * <p>Called on the OpMode thread from {@code observe()} / {@code start()} /
 * {@code stop()}. Must not block, write files, or command motors.
 */
public interface ElectricalObservationSink {
    void onObservation(ElectricalObservation observation, DriverTelemetry driver);

    void onLifecycle(String name);

    ElectricalObservationSink NOOP = new ElectricalObservationSink() {
        @Override
        public void onObservation(ElectricalObservation observation, DriverTelemetry driver) {
            // intentionally empty
        }

        @Override
        public void onLifecycle(String name) {
            // intentionally empty
        }
    };
}
