package org.allsparks.amper.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.allsparks.amper.AmperVersion;
import org.allsparks.amper.measure.CurrentSample;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.MeasurementValidity;
import org.allsparks.amper.measure.MotorSnapshot;
import org.allsparks.amper.measure.SamplingStats;
import org.allsparks.amper.measure.VoltageSample;
import org.allsparks.amper.telemetry.DriverTelemetry;
import org.allsparks.amper.api.DriverPowerState;

/**
 * Deterministic canonical-log example used by fixtures, tests, and the desktop
 * WPILOG converter. Not a Control Hub capture.
 */
public final class CanonicalLogExamples {
    private CanonicalLogExamples() {
    }

    public static SessionMetadata fixtureMetadata() {
        Map<String, String> extra = new LinkedHashMap<String, String>();
        extra.put("hardwarePlatform", "FTC_REV_HUB");
        return new SessionMetadata("fixture-session", "docs fixture", "CONSERVATIVE_PLACEHOLDER", extra);
    }

    public static CanonicalLog representativeSession() {
        CanonicalLog log = new CanonicalLog(32);
        CanonicalLogPublisher publisher = new CanonicalLogPublisher(log, fixtureMetadata());

        publisher.record(
                sample(0L, 12.73, 12.73, Double.NaN, 0.0, 0.0, MeasurementValidity.MISSING),
                driver(DriverPowerState.NORMAL),
                0L,
                null,
                null,
                Collections.<String>emptySet());
        publisher.record(
                sample(20_000_000L, 12.41, 12.50, 1.82, 0.50, 400.0, MeasurementValidity.VALID),
                driver(DriverPowerState.NORMAL),
                0L,
                "STATE_TRANSITION",
                "mechanism_start",
                Collections.<String>emptySet());
        publisher.record(
                sample(40_000_000L, Double.NaN, 12.48, Double.NaN, 0.50, 380.0, MeasurementValidity.MISSING),
                driver(DriverPowerState.SEVERE_VOLTAGE_RISK),
                0L,
                "VOLTAGE_WARNING",
                "sag, warning",
                Collections.<String>emptySet());
        publisher.record(
                sample(60_000_000L, 12.10, 12.20, 2.10, 0.0, 0.0, MeasurementValidity.VALID),
                driver(DriverPowerState.NORMAL),
                0L,
                "STATE_TRANSITION",
                "mechanism_stop",
                Collections.singleton("frontLeft"));
        return log;
    }

    private static DriverTelemetry driver(DriverPowerState state) {
        return new DriverTelemetry(state, true, state.name());
    }

    private static ElectricalObservation sample(
            long t,
            double busVolts,
            double filteredVolts,
            double currentAmps,
            double command,
            double velocity,
            MeasurementValidity currentValidity) {
        VoltageSample raw;
        if (Double.isNaN(busVolts)) {
            raw = VoltageSample.missing(t, "Control Hub");
        } else {
            raw = new VoltageSample(busVolts, t, MeasurementValidity.VALID, "Control Hub");
        }
        VoltageSample filtered = Double.isNaN(filteredVolts)
                ? VoltageSample.missing(t, "Control Hub")
                : new VoltageSample(filteredVolts, t, MeasurementValidity.VALID, "Control Hub");
        CurrentSample current;
        if (currentValidity == MeasurementValidity.MISSING || Double.isNaN(currentAmps)) {
            current = CurrentSample.missing(t, "frontLeft");
        } else {
            current = new CurrentSample(currentAmps, t, currentValidity, "frontLeft");
        }
        MotorSnapshot motor = new MotorSnapshot(
                "frontLeft", current, command, velocity, 0.0, current.isUsable(), Math.abs(command) >= 0.10);
        double minV = Double.isNaN(busVolts) ? 12.10 : Math.min(12.10, busVolts);
        if (t == 0L) {
            minV = busVolts;
        }
        return new ElectricalObservation(
                t,
                1_200_000L,
                raw,
                filtered,
                minV,
                CurrentSample.unsupported(t, "hub-battery"),
                Collections.singletonList(motor),
                raw.isUsable(),
                Collections.singletonList(raw),
                SamplingStats.empty(),
                false);
    }

    public static String tableCsv() {
        return AdvantageScopeCsv.table(representativeSession());
    }

    public static String schemaJson() {
        return LogSchemaSidecar.toJson(representativeSession(), fixtureMetadata());
    }

    public static String libraryVersion() {
        return AmperVersion.VERSION;
    }
}
