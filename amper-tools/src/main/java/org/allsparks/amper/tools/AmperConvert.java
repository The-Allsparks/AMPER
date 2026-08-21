package org.allsparks.amper.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import org.allsparks.amper.log.AdvantageScopeCsv;
import org.allsparks.amper.log.CanonicalLog;
import org.allsparks.amper.log.CanonicalLogExamples;
import org.allsparks.amper.log.CanonicalSample;
import org.allsparks.amper.log.FieldSpecs;
import org.allsparks.amper.log.LogFieldSpec;
import org.allsparks.amper.log.LogValue;
import org.allsparks.amper.log.LogValueType;
import org.allsparks.amper.tools.wpilog.WpiLogWriter;

/**
 * Desktop converters. Not an on-robot dependency.
 *
 * <pre>
 * amper-convert --to-wpilog &lt;advantagescope.csv&gt; &lt;out.wpilog&gt;
 * amper-convert --fixture-wpilog &lt;out.wpilog&gt;
 * </pre>
 */
public final class AmperConvert {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
            System.exit(2);
            return;
        }
        if ("--to-wpilog".equals(args[0]) && args.length >= 3) {
            Path csvPath = Paths.get(args[1]);
            Path outPath = Paths.get(args[2]);
            String csv = new String(Files.readAllBytes(csvPath), StandardCharsets.UTF_8);
            CanonicalLog log = fromTableCsv(csv);
            Path parent = outPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outPath, WpiLogWriter.toBytes(log));
            return;
        }
        if ("--write-fixtures".equals(args[0]) && args.length >= 2) {
            Path dir = Paths.get(args[1]);
            Files.createDirectories(dir);
            CanonicalLog log = CanonicalLogExamples.representativeSession();
            Files.write(
                    dir.resolve("amper-advantagescope-table.csv"),
                    org.allsparks.amper.log.AdvantageScopeCsv.table(log).getBytes(StandardCharsets.UTF_8));
            Files.write(
                    dir.resolve("amper-advantagescope-table.schema.json"),
                    org.allsparks.amper.log.LogSchemaSidecar.toJson(log, CanonicalLogExamples.fixtureMetadata())
                            .getBytes(StandardCharsets.UTF_8));
            Files.write(
                    dir.resolve("amper-advantagescope-list.csv"),
                    org.allsparks.amper.log.AdvantageScopeCsv.list(log).getBytes(StandardCharsets.UTF_8));
            return;
        }
        if ("--fixture-wpilog".equals(args[0]) && args.length >= 2) {
            Path outPath = Paths.get(args[1]);
            Path parent = outPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outPath, WpiLogWriter.toBytes(CanonicalLogExamples.representativeSession()));
            return;
        }
        usage();
        System.exit(2);
    }

    public static CanonicalLog fromTableCsv(String csv) {
        AdvantageScopeCsv.ParsedTable table = AdvantageScopeCsv.parseTable(csv);
        Map<String, LogValueType> types = inferTypes(table);
        CanonicalLog rebuilt = new CanonicalLog(Math.max(1, table.rows.size()));
        for (String key : table.keys) {
            LogValueType type = types.get(key);
            if (type == null) {
                type = LogValueType.STRING;
            }
            rebuilt.register(new LogFieldSpec(
                    key, type, "", "advantagescope-csv", false, false, "imported", "AMPER", FieldSpecs.VALID_OR_EMPTY));
        }
        for (AdvantageScopeCsv.ParsedRow row : table.rows) {
            long timestampNanos = Math.round(row.timestampAsSeconds() * 1_000_000_000.0);
            CanonicalSample.Builder builder = CanonicalSample.at(timestampNanos);
            for (String key : table.keys) {
                String cell = row.cells.get(key);
                LogValueType type = types.get(key);
                if (type == null || cell == null || cell.isEmpty()) {
                    continue;
                }
                builder.put(key, parseCell(type, cell));
            }
            rebuilt.append(builder.build());
        }
        return rebuilt;
    }

    static Map<String, LogValueType> inferTypes(AdvantageScopeCsv.ParsedTable table) {
        Map<String, LogValueType> types = new LinkedHashMap<String, LogValueType>();
        for (String key : table.keys) {
            LogValueType inferred = null;
            for (AdvantageScopeCsv.ParsedRow row : table.rows) {
                String cell = row.cells.get(key);
                if (cell == null || cell.isEmpty()) {
                    continue;
                }
                LogValueType cellType = inferCell(cell);
                if (inferred == null) {
                    inferred = cellType;
                } else if (inferred != cellType) {
                    inferred = LogValueType.STRING;
                }
            }
            types.put(key, inferred == null ? LogValueType.STRING : inferred);
        }
        return types;
    }

    static LogValueType inferCell(String cell) {
        if ("true".equalsIgnoreCase(cell) || "false".equalsIgnoreCase(cell)) {
            return LogValueType.BOOLEAN;
        }
        try {
            if (cell.indexOf('.') < 0 && cell.indexOf('e') < 0 && cell.indexOf('E') < 0) {
                Long.parseLong(cell);
                return LogValueType.INT64;
            }
            Double.parseDouble(cell);
            return LogValueType.DOUBLE;
        } catch (NumberFormatException ex) {
            return LogValueType.STRING;
        }
    }

    static LogValue parseCell(LogValueType type, String cell) {
        switch (type) {
            case BOOLEAN:
                return LogValue.ofBoolean("true".equalsIgnoreCase(cell));
            case DOUBLE:
                try {
                    return LogValue.ofDouble(Double.parseDouble(cell));
                } catch (NumberFormatException ex) {
                    return LogValue.missing(type);
                }
            case INT64:
                try {
                    return LogValue.ofInt64(Long.parseLong(cell));
                } catch (NumberFormatException ex) {
                    return LogValue.missing(type);
                }
            case STRING:
            default:
                return LogValue.ofString(cell);
        }
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("  amper-convert --to-wpilog <advantagescope.csv> <out.wpilog>");
        System.err.println("  amper-convert --write-fixtures <dir>");
        System.err.println("  amper-convert --fixture-wpilog <out.wpilog>");
    }
}
