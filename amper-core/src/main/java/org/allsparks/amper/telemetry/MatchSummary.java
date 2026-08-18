package org.allsparks.amper.telemetry;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.measure.LoopOverheadStats;

/** End-of-match (or on-demand) passive electrical summary. */
public final class MatchSummary {
    private final long sampleCount;
    private final double minVoltage;
    private final double maxVoltage;
    private final long maxLoopNanos;
    private final double meanLoopNanos;
    private final int mechanismStarts;
    private final int mechanismStops;
    private final int elevatedCount;
    private final int severeCount;
    private final int invalidSensingCount;
    private final int stallSuspicionCount;
    private final long droppedLogEvents;

    public MatchSummary(
            long sampleCount,
            double minVoltage,
            double maxVoltage,
            LoopOverheadStats loop,
            MechanismActivityTracker activity,
            DriverFeedback feedback,
            long droppedLogEvents) {
        this.sampleCount = sampleCount;
        this.minVoltage = minVoltage;
        this.maxVoltage = maxVoltage;
        this.maxLoopNanos = loop == null ? 0L : loop.maxNanos();
        this.meanLoopNanos = loop == null ? Double.NaN : loop.meanNanos();
        this.mechanismStarts = activity == null ? 0 : activity.startCount();
        this.mechanismStops = activity == null ? 0 : activity.stopCount();
        this.elevatedCount = feedback == null ? 0 : feedback.elevatedCount();
        this.severeCount = feedback == null ? 0 : feedback.severeCount();
        this.invalidSensingCount = feedback == null ? 0 : feedback.invalidCount();
        this.stallSuspicionCount = feedback == null ? 0 : feedback.stallCount();
        this.droppedLogEvents = droppedLogEvents;
    }

    public long sampleCount() {
        return sampleCount;
    }

    public double minVoltage() {
        return minVoltage;
    }

    public double maxVoltage() {
        return maxVoltage;
    }

    public long maxLoopNanos() {
        return maxLoopNanos;
    }

    public double meanLoopNanos() {
        return meanLoopNanos;
    }

    public int mechanismStarts() {
        return mechanismStarts;
    }

    public int mechanismStops() {
        return mechanismStops;
    }

    public PowerEvent toEvent(long timestampNanos) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("samples", Long.toString(sampleCount));
        fields.put("minV", format(minVoltage));
        fields.put("maxV", format(maxVoltage));
        fields.put("maxLoopNs", Long.toString(maxLoopNanos));
        fields.put("meanLoopNs", format(meanLoopNanos));
        fields.put("starts", Integer.toString(mechanismStarts));
        fields.put("stops", Integer.toString(mechanismStops));
        fields.put("elevated", Integer.toString(elevatedCount));
        fields.put("severe", Integer.toString(severeCount));
        fields.put("invalid", Integer.toString(invalidSensingCount));
        fields.put("stall", Integer.toString(stallSuspicionCount));
        fields.put("droppedLogs", Long.toString(droppedLogEvents));
        return new PowerEvent(timestampNanos, PowerEventType.MATCH_SUMMARY, "match_summary", fields);
    }

    public String toDriverLines() {
        return "AMPER samples=" + sampleCount
                + " minV=" + format(minVoltage)
                + " maxV=" + format(maxVoltage)
                + " maxLoopUs=" + (maxLoopNanos / 1000L)
                + " starts=" + mechanismStarts
                + " severeWarn=" + severeCount;
    }

    private static String format(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        return String.format(Locale.US, "%.3f", value);
    }
}
