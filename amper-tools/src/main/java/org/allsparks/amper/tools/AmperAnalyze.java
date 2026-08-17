package org.allsparks.amper.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.allsparks.amper.log.PowerEvent;
import org.allsparks.amper.log.PowerEventType;
import org.allsparks.amper.sim.CsvReplay;

/**
 * Desktop analysis for AMPER CSV exports. Not an on-robot dependency.
 *
 * <p>Usage: {@code java -jar amper-tools.jar path/to/amper-session.csv}
 */
public final class AmperAnalyze {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: amper-analyze <amper-session.csv> [output.md]");
            System.exit(2);
            return;
        }
        Path input = Paths.get(args[0]);
        String csv = new String(Files.readAllBytes(input), StandardCharsets.UTF_8);
        String report = analyze(csv);
        if (args.length >= 2) {
            Files.write(Paths.get(args[1]), report.getBytes(StandardCharsets.UTF_8));
        }
        System.out.print(report);
    }

    public static String analyze(String csv) {
        List<PowerEvent> events = CsvReplay.parse(csv);
        List<Sample> samples = new ArrayList<Sample>();
        List<PowerEvent> markers = new ArrayList<PowerEvent>();
        for (PowerEvent event : events) {
            if (event.type() == PowerEventType.LOOP_SAMPLE || event.type() == PowerEventType.SENSOR_INVALID) {
                samples.add(Sample.from(event));
            } else {
                markers.add(event);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("# AMPER desktop analysis\n\n");
        sb.append("This report is **software analysis of a CSV**, not Control Hub validation.\n\n");
        sb.append("- samples: ").append(samples.size()).append('\n');
        sb.append("- other events: ").append(markers.size()).append('\n');
        if (samples.isEmpty()) {
            sb.append("\nNo LOOP_SAMPLE rows found.\n");
            return sb.toString();
        }
        double minV = Double.POSITIVE_INFINITY;
        double maxV = Double.NEGATIVE_INFINITY;
        long maxLoop = 0L;
        long sumLoop = 0L;
        int valid = 0;
        for (Sample sample : samples) {
            if (!Double.isNaN(sample.rawV)) {
                minV = Math.min(minV, sample.rawV);
                maxV = Math.max(maxV, sample.rawV);
            }
            if (!Double.isNaN(sample.filtV)) {
                minV = Math.min(minV, sample.filtV);
                maxV = Math.max(maxV, sample.filtV);
            }
            maxLoop = Math.max(maxLoop, sample.loopNs);
            sumLoop += sample.loopNs;
            if (sample.valid) {
                valid++;
            }
        }
        sb.append(String.format(Locale.US, "- raw/filt voltage min: %.4f\n", minV));
        sb.append(String.format(Locale.US, "- raw/filt voltage max: %.4f\n", maxV));
        sb.append(String.format(Locale.US, "- mean AMPER update ns: %.1f\n", (double) sumLoop / samples.size()));
        sb.append("- max AMPER update ns: ").append(maxLoop).append('\n');
        sb.append("- valid samples: ").append(valid).append('/').append(samples.size()).append('\n');
        sb.append("\n## Voltage vs time\n\n");
        sb.append("| t_ns | raw_v | filt_v | valid | sumAbsCmd |\n|---|---|---|---|---|\n");
        int rows = Math.min(samples.size(), 40);
        for (int i = 0; i < rows; i++) {
            Sample sample = samples.get(i);
            sb.append("| ").append(sample.t)
                    .append(" | ").append(fmt(sample.rawV))
                    .append(" | ").append(fmt(sample.filtV))
                    .append(" | ").append(sample.valid)
                    .append(" | ").append(fmt(sample.sumAbsCmd))
                    .append(" |\n");
        }
        if (samples.size() > rows) {
            sb.append("| ... | ... | ... | ... | ... |\n");
        }
        sb.append("\n## Command / activity markers\n\n");
        int shown = 0;
        for (PowerEvent event : markers) {
            if (event.type() == PowerEventType.STATE_TRANSITION
                    || event.type() == PowerEventType.STALL_SUSPECTED
                    || event.type() == PowerEventType.VOLTAGE_WARNING) {
                sb.append("- t=").append(event.timestampNanos())
                        .append(" ").append(event.type())
                        .append(" ").append(event.message())
                        .append('\n');
                shown++;
            }
        }
        if (shown == 0) {
            sb.append("_none_\n");
        }
        sb.append("\n## Current traces (when present)\n\n");
        Sample first = samples.get(0);
        if (first.m0A != null) {
            sb.append("| t_ns | m0_A | m0_validity |\n|---|---|---|\n");
            for (int i = 0; i < rows; i++) {
                Sample sample = samples.get(i);
                sb.append("| ").append(sample.t)
                        .append(" | ").append(sample.m0A)
                        .append(" | ").append(sample.m0Validity)
                        .append(" |\n");
            }
        } else {
            sb.append("_no motor current columns in this file_\n");
        }
        sb.append("\n## Loop overhead summary\n\n");
        sb.append("Use max/mean AMPER update ns above. Compare against your OpMode loop time on the Control Hub.\n");
        return sb.toString();
    }

    private static String fmt(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        return String.format(Locale.US, "%.4f", value);
    }

    private static final class Sample {
        long t;
        double rawV = Double.NaN;
        double filtV = Double.NaN;
        boolean valid;
        double sumAbsCmd = Double.NaN;
        long loopNs;
        String m0A;
        String m0Validity;

        static Sample from(PowerEvent event) {
            Sample sample = new Sample();
            sample.t = event.timestampNanos();
            sample.rawV = parseDouble(event.fields().get("rawV"));
            sample.filtV = parseDouble(event.fields().get("filtV"));
            sample.valid = "true".equalsIgnoreCase(event.fields().get("sensingValid"));
            sample.sumAbsCmd = parseDouble(event.fields().get("sumAbsCmd"));
            sample.loopNs = parseLong(event.fields().get("loopNs"));
            sample.m0A = event.fields().get("m0A");
            sample.m0Validity = event.fields().get("m0Validity");
            return sample;
        }

        private static double parseDouble(String raw) {
            if (raw == null || raw.isEmpty() || "NaN".equals(raw)) {
                return Double.NaN;
            }
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }

        private static long parseLong(String raw) {
            if (raw == null || raw.isEmpty()) {
                return 0L;
            }
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException ex) {
                return 0L;
            }
        }
    }
}
