package org.allsparks.amper;

import java.util.List;
import java.util.Objects;
import org.allsparks.amper.api.DriverPowerState;
import org.allsparks.amper.battery.BatteryEstimator;
import org.allsparks.amper.battery.BatteryObservation;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.clock.SystemNanoClock;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.measure.ElectricalObservation;
import org.allsparks.amper.measure.LoopOverheadStats;
import org.allsparks.amper.measure.MotorElectricalTelemetry;
import org.allsparks.amper.measure.PowerMonitor;
import org.allsparks.amper.measure.PowerTelemetrySource;
import org.allsparks.amper.policy.PowerPolicy;
import org.allsparks.amper.telemetry.DriverFeedback;
import org.allsparks.amper.telemetry.DriverTelemetry;
import org.allsparks.amper.telemetry.MatchSummary;
import org.allsparks.amper.telemetry.MechanismActivityTracker;
import org.allsparks.amper.telemetry.StallSuspicionTracker;

/**
 * Per-OpMode AMPER session. Observes and logs; never commands motors.
 *
 * <p>Phase 0 always samples. Phase 1 extras (warnings, start/stop, summaries)
 * run only when {@link AmperFeatureFlags#isPhase1PassiveTelemetry()} is true.
 */
public final class AmperSession {
    private final PowerPolicy policy;
    private final PowerMonitor monitor;
    private final PowerEventLogger logger;
    private final BatteryEstimator batteryEstimator;
    private final MechanismActivityTracker activityTracker;
    private final StallSuspicionTracker stallTracker;
    private final DriverFeedback driverFeedback;
    private final LoopOverheadStats loopStats;
    private long samples;
    private DriverTelemetry lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "NORMAL");
    private BatteryObservation lastBattery;

    public AmperSession(
            PowerPolicy policy,
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.monitor = PowerMonitor.create(clock, telemetrySource, motors, policy);
        this.logger = new PowerEventLogger(policy.loggerCapacity());
        this.batteryEstimator = new BatteryEstimator();
        this.activityTracker = new MechanismActivityTracker();
        this.stallTracker = new StallSuspicionTracker();
        this.driverFeedback = new DriverFeedback();
        this.loopStats = new LoopOverheadStats();
    }

    public static AmperSession create(
            PowerPolicy policy,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors) {
        return new AmperSession(policy, new SystemNanoClock(), telemetrySource, motors);
    }

    /**
     * Read sensors, update filters, and optionally emit Phase 1 events.
     * Call once per robot loop. Does not write motor outputs.
     */
    public ElectricalObservation observe() {
        ElectricalObservation observation = monitor.update();
        samples++;
        loopStats.offer(observation.loopDurationNanos());
        logger.recordObservation(observation);
        lastBattery = batteryEstimator.update(observation);

        if (policy.featureFlags().isPhase1PassiveTelemetry()) {
            activityTracker.update(observation, policy, logger);
            boolean stall = stallTracker.update(observation, policy, logger);
            lastDriver = driverFeedback.update(observation, lastBattery, stall, policy, logger);
        } else {
            lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "PHASE1_DISABLED");
        }
        return observation;
    }

    public DriverTelemetry driverTelemetry() {
        return lastDriver;
    }

    public BatteryObservation batteryObservation() {
        return lastBattery;
    }

    public MatchSummary matchSummary() {
        ElectricalObservation last = monitor.lastObservation();
        double minV = last == null ? Double.NaN : last.voltageMinimumThisMatch();
        double maxV = lastBattery == null ? Double.NaN : lastBattery.restingHintVolts();
        return new MatchSummary(
                samples,
                minV,
                maxV,
                loopStats,
                activityTracker,
                driverFeedback,
                logger.droppedCount());
    }

    public void recordMatchSummary() {
        MatchSummary summary = matchSummary();
        long timestamp = monitor.lastObservation() == null ? 0L : monitor.lastObservation().loopStartNanos();
        logger.record(summary.toEvent(timestamp));
    }

    public String exportCsv() {
        return logger.exportCsv();
    }

    public PowerEventLogger logger() {
        return logger;
    }

    public PowerMonitor monitor() {
        return monitor;
    }

    public LoopOverheadStats loopOverhead() {
        return loopStats;
    }

    public PowerPolicy policy() {
        return policy;
    }

    public void resetMatch() {
        samples = 0L;
        monitor.resetMatchStatistics();
        batteryEstimator.reset();
        activityTracker.reset();
        stallTracker.reset();
        driverFeedback.reset();
        loopStats.reset();
        logger.clear();
        lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "NORMAL");
        lastBattery = null;
    }
}
