package org.allsparks.amper.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.allsparks.amper.AmperFeatureFlags;
import org.allsparks.amper.AmperSession;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.policy.SamplingPolicy;

/**
 * Deterministic traces for software tests. Results are <strong>not</strong>
 * hardware validation.
 */
public final class TraceGenerator {
    private TraceGenerator() {
    }

    public static List<ElectricalObservation> healthyBattery() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        SimulatedMotor drive = new SimulatedMotor("drive");
        drive.setCurrentAmps(2.0);
        drive.setVelocity(400.0);
        AmperSession session = session(clock, hub, drive);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 40; i++) {
            hub.setVolts(12.5 - 0.01 * i);
            drive.setCommand(i > 5 ? 0.4 : 0.0);
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> weakHighResistanceBattery() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        SimulatedMotor drive = new SimulatedMotor("drive");
        drive.setCurrentAmps(8.0);
        drive.setVelocity(200.0);
        AmperSession session = session(clock, hub, drive);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 40; i++) {
            double load = i > 8 ? 1.0 : 0.0;
            drive.setCommand(load);
            hub.setVolts(12.2 - load * 2.4 - 0.02 * i);
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> voltageNoise() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        AmperSession session = session(clock, hub, null);
        Random random = new Random(1L);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 30; i++) {
            hub.setVolts(12.4 + (random.nextDouble() - 0.5) * 0.3);
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> missingSamples() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        AmperSession session = session(clock, hub, null);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 10; i++) {
            if (i == 4 || i == 5) {
                hub.setMissing();
            } else {
                hub.setVolts(12.3);
            }
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> staleSamples() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        hub.setVolts(12.1);
        hub.setStaleOffset(500_000_000L);
        AmperSession session = session(clock, hub, null);
        clock.set(1_000_000L);
        return Collections.singletonList(session.observe());
    }

    public static List<ElectricalObservation> stalledIntake() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        SimulatedMotor intake = new SimulatedMotor("intake");
        intake.setCurrentAmps(12.0);
        intake.setVelocity(5.0);
        intake.setCommand(1.0);
        AmperSession session = session(clock, hub, intake);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 20; i++) {
            hub.setVolts(11.8);
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> simultaneousMechanismStarts() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        SimulatedMotor drive = new SimulatedMotor("drive");
        SimulatedMotor lift = new SimulatedMotor("lift");
        drive.setCurrentAmps(6.0);
        lift.setCurrentAmps(5.0);
        drive.setVelocity(300.0);
        lift.setVelocity(100.0);
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.everyLoop())
                .build();
        AmperSession session = new AmperSession(
                policy,
                clock,
                hub,
                java.util.Arrays.<org.allsparks.amper.measure.MotorElectricalTelemetry>asList(drive, lift));
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        for (int i = 0; i < 12; i++) {
            double cmd = i >= 2 ? 0.8 : 0.0;
            drive.setCommand(cmd);
            lift.setCommand(cmd);
            hub.setVolts(12.4 - cmd * 1.2);
            clock.set(i * 20_000_000L);
            out.add(session.observe());
        }
        return out;
    }

    public static List<ElectricalObservation> loopTimingSpikes() {
        SimulatedClock clock = new SimulatedClock();
        SimulatedHubSource hub = new SimulatedHubSource("Control Hub");
        AmperSession session = session(clock, hub, null);
        List<ElectricalObservation> out = new ArrayList<ElectricalObservation>();
        long t = 0L;
        for (int i = 0; i < 15; i++) {
            hub.setVolts(12.4);
            clock.set(t);
            ElectricalObservation obs = session.observe();
            out.add(obs);
            t += (i == 8) ? 40_000_000L : 20_000_000L;
        }
        return out;
    }

    private static AmperSession session(SimulatedClock clock, SimulatedHubSource hub, SimulatedMotor motor) {
        PowerPolicy policy = PowerPolicy.builder()
                .featureFlags(AmperFeatureFlags.passiveTelemetry())
                .sampling(SamplingPolicy.everyLoop())
                .build();
        List<org.allsparks.amper.measure.MotorElectricalTelemetry> motors = motor == null
                ? Collections.<org.allsparks.amper.measure.MotorElectricalTelemetry>emptyList()
                : Collections.<org.allsparks.amper.measure.MotorElectricalTelemetry>singletonList(motor);
        return new AmperSession(policy, clock, hub, motors);
    }
}
