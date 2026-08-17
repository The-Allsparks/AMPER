package org.allsparks.amper;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.allsparks.amper.api.DriverPowerState;
import org.allsparks.amper.battery.BatteryEstimator;
import org.allsparks.amper.battery.BatteryObservation;
import org.allsparks.amper.clock.AmperClock;
import org.allsparks.amper.clock.SystemNanoClock;
import org.allsparks.amper.log.AdvantageScopeCsv;
import org.allsparks.amper.log.CanonicalLog;
import org.allsparks.amper.log.CanonicalLogPublisher;
import org.allsparks.amper.log.CsvFormat;
import org.allsparks.amper.log.LogNameSanitizer;
import org.allsparks.amper.log.LogSchemaSidecar;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventLogger;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.log.SessionLogSink;
import org.allsparks.amper.log.SessionMetadata;
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
import org.allsparks.amper.telemetry.TelemetrySink;

/**
 * Per-OpMode AMPER session. Observes and logs; never commands motors.
 *
 * <p>Phase 0 samples when {@link AmperFeatureFlags#isPhase0Measurement()} is true.
 * Phase 1 extras (warnings, start/stop, summaries) run only when
 * {@link AmperFeatureFlags#isPhase1PassiveTelemetry()} is true.
 *
 * <p>Call {@link #initialize()} from OpMode {@code init}, {@link #start()} from
 * {@code start}/{@code waitForStart}, {@link #observe()} once per control loop,
 * {@link #publishTelemetry(TelemetrySink)} when Driver Station lines are due,
 * and {@link #stop()} from {@code stop}.
 */
public final class AmperSession {
    private final PowerPolicy policy;
    private final AmperClock clock;
    private final PowerMonitor monitor;
    private final PowerEventLogger logger;
    private final BatteryEstimator batteryEstimator;
    private final MechanismActivityTracker activityTracker;
    private final StallSuspicionTracker stallTracker;
    private final DriverFeedback driverFeedback;
    private final LoopOverheadStats loopStats;
    private final SessionLogSink logSink;
    private final String exportFilename;
    private final CanonicalLog canonicalLog;
    private final CanonicalLogPublisher canonicalPublisher;
    private long samples;
    private DriverTelemetry lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "NORMAL");
    private BatteryObservation lastBattery;
    private AmperLifecycle lifecycle = AmperLifecycle.CONSTRUCTED;
    private int duplicateObserves;
    private int sinkFailures;
    private ElectricalObservation lastObservation;

    public AmperSession(
            PowerPolicy policy,
            AmperClock clock,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors) {
        this(policy, clock, Collections.singletonList(telemetrySource), 0, motors, null, null, null);
    }

    public AmperSession(
            PowerPolicy policy,
            AmperClock clock,
            List<PowerTelemetrySource> telemetrySources,
            int policySourceIndex,
            List<MotorElectricalTelemetry> motors,
            SessionMetadata metadata,
            SessionLogSink logSink,
            String exportFilename) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = clock == null ? new SystemNanoClock() : clock;
        this.monitor = PowerMonitor.create(
                this.clock, telemetrySources, policySourceIndex, motors, policy);
        SessionMetadata meta = metadata == null
                ? SessionMetadata.anonymous(policy.voltageThresholdProvenance().name())
                : metadata;
        this.logger = new PowerEventLogger(policy.loggerCapacity(), meta);
        this.batteryEstimator = new BatteryEstimator();
        this.activityTracker = new MechanismActivityTracker();
        this.stallTracker = new StallSuspicionTracker();
        this.driverFeedback = new DriverFeedback();
        this.loopStats = new LoopOverheadStats();
        this.logSink = logSink;
        this.exportFilename = exportFilename == null || exportFilename.trim().isEmpty()
                ? "amper-session.csv"
                : exportFilename;
        this.canonicalLog = new CanonicalLog(policy.loggerCapacity(), new LogNameSanitizer());
        this.canonicalPublisher = new CanonicalLogPublisher(this.canonicalLog, meta);
    }

    public static AmperSession create(
            PowerPolicy policy,
            PowerTelemetrySource telemetrySource,
            List<MotorElectricalTelemetry> motors) {
        return new AmperSession(policy, new SystemNanoClock(), telemetrySource, motors);
    }

    /** Optional init-time probe. Safe to call more than once. */
    public void initialize() {
        if (lifecycle == AmperLifecycle.CLOSED) {
            throw new IllegalStateException("AMPER session is closed");
        }
        if (lifecycle == AmperLifecycle.CONSTRUCTED || lifecycle == AmperLifecycle.STOPPED) {
            lifecycle = AmperLifecycle.INITIALIZED;
            recordLifecycle("initialize");
        }
    }

    /** Reset match statistics at play start. Safe to call more than once. */
    public void start() {
        if (lifecycle == AmperLifecycle.CLOSED) {
            throw new IllegalStateException("AMPER session is closed");
        }
        resetMatch();
        lifecycle = AmperLifecycle.STARTED;
        recordLifecycle("start");
    }

    /**
     * Read sensors, update filters, and optionally emit Phase 1 events.
     * Call once per robot loop. Does not write motor outputs.
     *
     * <p>A second call inside {@link org.allsparks.amper.policy.SamplingPolicy#duplicateObserveWindowNanos()}
     * returns the previous observation and records {@code DUPLICATE_OBSERVE}.
     */
    public ElectricalObservation observe() {
        if (lifecycle == AmperLifecycle.CLOSED) {
            throw new IllegalStateException("AMPER session is closed");
        }
        if (lifecycle == AmperLifecycle.CONSTRUCTED) {
            initialize();
        }
        if (lifecycle == AmperLifecycle.INITIALIZED || lifecycle == AmperLifecycle.STOPPED) {
            lifecycle = AmperLifecycle.STARTED;
        }
        if (!policy.featureFlags().isPhase0Measurement()) {
            lastObservation = ElectricalObservation.disabled(clock.nanoTime());
            lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "AMPER_DISABLED");
            return lastObservation;
        }
        long now = clock.nanoTime();
        if (lastObservation != null
                && now - lastObservation.loopStartNanos() < policy.sampling().duplicateObserveWindowNanos()) {
            duplicateObserves++;
            Map<String, String> fields = new LinkedHashMap<String, String>();
            fields.put("count", Integer.toString(duplicateObserves));
            logger.record(new PowerEvent(now, PowerEventType.DUPLICATE_OBSERVE, "duplicate_observe", fields));
            return lastObservation;
        }

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
        publishCanonical(observation);
        lastObservation = observation;
        return observation;
    }

    /** Alias for {@link #observe()} for command-scheduler style loops. */
    public ElectricalObservation update() {
        return observe();
    }

    /**
     * Publish rate-limited Driver Station lines. Call after {@link #observe()}.
     * Does nothing when AMPER or Phase 1 is disabled except a one-line state.
     */
    public void publishTelemetry(TelemetrySink telemetry) {
        if (telemetry == null) {
            return;
        }
        DriverTelemetry driver = lastDriver;
        if (driver.publishedThisCycle() || lastObservation == null) {
            telemetry.addData("AMPER", driver.message());
            if (lastObservation != null && !lastObservation.disabled()) {
                telemetry.addData("AMPER.V", lastObservation.filteredVoltage().volts());
                telemetry.addData("AMPER.valid", lastObservation.sensingValid());
                telemetry.addData("AMPER.loopUs", lastObservation.loopDurationNanos() / 1000L);
            }
            telemetry.update();
        }
    }

    public DriverTelemetry driverTelemetry() {
        return lastDriver;
    }

    public BatteryObservation batteryObservation() {
        return lastBattery;
    }

    public MatchSummary matchSummary() {
        ElectricalObservation last = monitor.lastObservation();
        if (last == null) {
            last = lastObservation;
        }
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
        long timestamp = lastObservation == null ? clock.nanoTime() : lastObservation.loopStartNanos();
        logger.record(summary.toEvent(timestamp));
    }

    public String exportCsv() {
        return logger.exportCsv();
    }

    /** AdvantageScope table-layout CSV. Primary robot-side export. */
    public String exportAdvantageScopeTableCsv() {
        return AdvantageScopeCsv.table(canonicalLog);
    }

    /** AdvantageScope list-layout CSV. */
    public String exportAdvantageScopeListCsv() {
        return AdvantageScopeCsv.list(canonicalLog);
    }

    public String exportSchemaSidecar() {
        return LogSchemaSidecar.toJson(canonicalLog, logger.metadata());
    }

    /**
     * Record the match summary and write CSV through the configured sink.
     * Safe to call more than once; subsequent calls are no-ops after close.
     */
    public void stop() {
        if (lifecycle == AmperLifecycle.CLOSED || lifecycle == AmperLifecycle.STOPPED) {
            return;
        }
        if (policy.featureFlags().isPhase0Measurement()) {
            recordMatchSummary();
            flushLog();
        }
        lifecycle = AmperLifecycle.STOPPED;
        recordLifecycle("stop");
    }

    public void close() {
        if (lifecycle != AmperLifecycle.STOPPED && lifecycle != AmperLifecycle.CLOSED) {
            stop();
        }
        lifecycle = AmperLifecycle.CLOSED;
    }

    public void flushLog() {
        if (logSink == null || logger.exported()) {
            return;
        }
        boolean csvOk = false;
        try {
            logSink.export(CsvFormat.sanitizeLeaf(exportFilename), exportAdvantageScopeTableCsv());
            csvOk = true;
        } catch (IOException ex) {
            recordSinkFailure(ex);
        }
        try {
            logSink.export(CsvFormat.sidecarFilename(exportFilename), exportSchemaSidecar());
        } catch (IOException ex) {
            recordSinkFailure(ex);
        }
        if (csvOk) {
            logger.markExported();
        }
    }

    public PowerEventLogger logger() {
        return logger;
    }

    public CanonicalLog canonicalLog() {
        return canonicalLog;
    }

    public int sinkFailureCount() {
        return sinkFailures;
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

    public AmperLifecycle lifecycle() {
        return lifecycle;
    }

    public int duplicateObserveCount() {
        return duplicateObserves;
    }

    public void resetMatch() {
        samples = 0L;
        duplicateObserves = 0;
        monitor.resetMatchStatistics();
        batteryEstimator.reset();
        activityTracker.reset();
        stallTracker.reset();
        driverFeedback.reset();
        loopStats.reset();
        logger.clear();
        canonicalPublisher.reset();
        lastDriver = new DriverTelemetry(DriverPowerState.NORMAL, false, "NORMAL");
        lastBattery = null;
        lastObservation = null;
        sinkFailures = 0;
    }

    private void publishCanonical(ElectricalObservation observation) {
        String eventType = null;
        String eventMessage = null;
        for (PowerEvent event : logger.snapshot()) {
            if (event.timestampNanos() == observation.loopStartNanos()
                    && event.type() != PowerEventType.LOOP_SAMPLE
                    && event.type() != PowerEventType.SENSOR_INVALID) {
                eventType = event.type().name();
                eventMessage = event.message();
            }
        }
        canonicalPublisher.record(
                observation,
                lastDriver,
                logger.droppedCount() + canonicalLog.droppedCount(),
                eventType,
                eventMessage,
                stallTracker.suspectedMotorIds());
    }

    private void recordSinkFailure(Exception ex) {
        sinkFailures++;
        Map<String, String> fields = new LinkedHashMap<String, String>();
        fields.put("error", ex.getClass().getSimpleName());
        fields.put("count", Integer.toString(sinkFailures));
        logger.record(new PowerEvent(clock.nanoTime(), PowerEventType.EXPORT, "export_failed", fields));
    }

    private void recordLifecycle(String name) {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        fields.put("lifecycle", lifecycle.name());
        logger.record(new PowerEvent(clock.nanoTime(), PowerEventType.LIFECYCLE, name, fields));
    }
}
