package org.allsparks.amper.arch;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Executable package boundaries. Architecture lives in documentation
 * <em>and</em> these tests so new AI-generated imports fail CI.
 */
class PackageBoundaryTest {

    private static final Set<String> FORBIDDEN_CORE_PREFIXES = new HashSet<String>(Arrays.asList(
            "com.qualcomm.",
            "org.firstinspires.ftc.",
            "android.",
            "org.allsparks.amper.ftc.",
            "org.allsparks.amper.tools."));

    @Test
    void coreDoesNotImportFtcAndroidOrDesktopTools() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("import ")) {
                    continue;
                }
                String imported = stripImport(line);
                for (String prefix : FORBIDDEN_CORE_PREFIXES) {
                    if (imported.startsWith(prefix)) {
                        hits.add(rel(main, path) + ":" + (i + 1) + " " + imported);
                    }
                }
            }
        }
        failIf(hits, "amper-core imported FTC, Android, amper-ftc, or amper-tools");
    }

    @Test
    void productionPackagesDoNotImportSim() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String pkg = SourceScan.packageOf(path, main);
            if (pkg.contains(".sim")) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("import org.allsparks.amper.sim.")) {
                    hits.add(rel(main, path) + ":" + (i + 1));
                }
            }
        }
        failIf(hits, "non-sim production code imported org.allsparks.amper.sim");
    }

    @Test
    void measureFilterClockDoNotDependOnInterventionOrLogging() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        Set<String> watched = new HashSet<String>(Arrays.asList("measure", "filter", "clock"));
        Set<String> forbidden = new HashSet<String>(Arrays.asList(
                "coord", "predict", "protect", "sim", "telemetry", "log", "adapters"));
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.amperChildPackage(SourceScan.packageOf(path, main));
            if (!watched.contains(child)) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("import org.allsparks.amper.")) {
                    continue;
                }
                String imported = stripImport(line);
                String importedChild = SourceScan.amperChildPackage(packageNameOf(imported));
                if (forbidden.contains(importedChild)) {
                    hits.add(rel(main, path) + ":" + (i + 1) + " -> " + imported);
                }
            }
        }
        failIf(hits, "measure/filter/clock imported intervention, sim, telemetry, or log types");
    }

    @Test
    void policyDoesNotDependOnHardwareOrIntervention() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        Set<String> forbidden = new HashSet<String>(Arrays.asList(
                "measure", "protect", "coord", "predict", "sim", "log", "telemetry", "adapters"));
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.amperChildPackage(SourceScan.packageOf(path, main));
            if (!"policy".equals(child)) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("import org.allsparks.amper.")) {
                    continue;
                }
                String imported = stripImport(line);
                String importedChild = SourceScan.amperChildPackage(packageNameOf(imported));
                if (forbidden.contains(importedChild)) {
                    hits.add(rel(main, path) + ":" + (i + 1) + " -> " + imported);
                }
            }
        }
        failIf(hits, "policy imported measure/hardware/intervention packages");
    }

    private static String stripImport(String line) {
        String imported = line.substring("import ".length()).trim();
        if (imported.endsWith(";")) {
            imported = imported.substring(0, imported.length() - 1).trim();
        }
        if (imported.startsWith("static ")) {
            imported = imported.substring("static ".length()).trim();
        }
        return imported;
    }

    private static String packageNameOf(String importedType) {
        int lastDot = importedType.lastIndexOf('.');
        if (lastDot < 0) {
            return importedType;
        }
        return importedType.substring(0, lastDot);
    }

    private static String rel(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    private static void failIf(List<String> hits, String message) {
        if (!hits.isEmpty()) {
            fail(message + ":\n" + String.join("\n", hits));
        }
    }
}
