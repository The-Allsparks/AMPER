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
 * Static guards for latency-sensitive packages. These are not Hub timings;
 * they prevent known blocking and allocation-heavy APIs from entering the
 * robot observation path.
 */
class HotPathGuardTest {

    private static final Set<String> HOT_PACKAGES = new HashSet<String>(
            Arrays.asList("measure", "filter", "battery", "telemetry", "clock", "protect", "coord", "predict"));

    private static final String[] FORBIDDEN_SNIPPETS = {
        "Thread.sleep",
        "java.net.",
        "java.nio.file",
        "FileOutputStream",
        "FileWriter",
        "Socket ",
        "HttpURLConnection",
        "Executors.",
        "new Thread("
    };

    @Test
    void hotPackagesDoNotBlockOrStartThreads() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        for (Path path : SourceScan.javaFiles(main)) {
            String child = SourceScan.amperChildPackage(SourceScan.packageOf(path, main));
            if (!HOT_PACKAGES.contains(child)) {
                continue;
            }
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String trimmed = line.trim();
                if (isComment(trimmed)) {
                    continue;
                }
                for (int s = 0; s < FORBIDDEN_SNIPPETS.length; s++) {
                    if (trimmed.contains(FORBIDDEN_SNIPPETS[s])) {
                        hits.add(rel(main, path) + ":" + (i + 1) + " " + FORBIDDEN_SNIPPETS[s]);
                    }
                }
            }
        }
        failIf(hits, "hot-path package used blocking I/O, networking, sleep, or extra threads");
    }

    @Test
    void observePathDoesNotUseStreams() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        String[] watched = {
            "org/allsparks/amper/AmperSession.java",
            "org/allsparks/amper/measure/PowerMonitor.java",
            "org/allsparks/amper/log/CanonicalLog.java",
            "org/allsparks/amper/log/CanonicalLogPublisher.java",
            "org/allsparks/amper/log/PowerEventLogger.java"
        };
        for (int w = 0; w < watched.length; w++) {
            Path path = main.resolve(watched[w]);
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (isComment(trimmed)) {
                    continue;
                }
                if (trimmed.contains(".stream(") || trimmed.contains("Collectors.")) {
                    hits.add(watched[w] + ":" + (i + 1));
                }
            }
        }
        failIf(hits, "control-loop classes used Stream/Collectors (allocating)");
    }

    @Test
    void observeHotPathDoesNotAllocateScratchCollections() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        rejectAfterMethod(
                hits,
                main.resolve("org/allsparks/amper/measure/PowerMonitor.java"),
                "public ElectricalObservation update()",
                new String[] {"new ArrayList", "new LinkedHashMap", "new boolean[", "new DoubleRead"});
        rejectAfterMethod(
                hits,
                main.resolve("org/allsparks/amper/log/PowerEventLogger.java"),
                "public void recordObservation(",
                new String[] {"new LinkedHashMap", "String.format"});
        failIf(hits, "observe hot path allocated a fresh list/map/format buffer");
    }

    @Test
    void logsDoNotShiftOnOverflow() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path main = SourceScan.coreMain();
        String[] watched = {"org/allsparks/amper/log/CanonicalLog.java", "org/allsparks/amper/log/PowerEventLogger.java"
        };
        for (int w = 0; w < watched.length; w++) {
            Path path = main.resolve(watched[w]);
            String[] lines = SourceScan.read(path).split("\n");
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (isComment(trimmed)) {
                    continue;
                }
                if (trimmed.contains(".remove(0)")) {
                    hits.add(watched[w] + ":" + (i + 1));
                }
            }
        }
        failIf(hits, "bounded logs used ArrayList.remove(0) on overflow");
    }

    @Test
    void sessionObserveDoesNotSleepOrWriteFilesInline() throws IOException {
        List<String> hits = new ArrayList<String>();
        Path path = SourceScan.coreMain().resolve("org/allsparks/amper/AmperSession.java");
        String[] lines = SourceScan.read(path).split("\n");
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (isComment(trimmed)) {
                continue;
            }
            if (trimmed.contains("Thread.sleep")
                    || trimmed.contains("FileOutputStream")
                    || trimmed.contains("new FileWriter")) {
                hits.add("AmperSession.java:" + (i + 1) + " " + trimmed);
            }
        }
        failIf(hits, "AmperSession used sleep or inline file writes");
    }

    private static void rejectAfterMethod(List<String> hits, Path path, String methodSig, String[] snippets) {
        String[] lines = SourceScan.read(path).split("\n");
        boolean inMethod = false;
        int depth = 0;
        String name = path.getFileName().toString();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!inMethod) {
                if (trimmed.contains(methodSig)) {
                    inMethod = true;
                    depth = braceDelta(lines[i]);
                }
                continue;
            }
            depth += braceDelta(lines[i]);
            if (isComment(trimmed)) {
                if (depth <= 0) {
                    break;
                }
                continue;
            }
            for (int s = 0; s < snippets.length; s++) {
                if (trimmed.contains(snippets[s])) {
                    hits.add(name + ":" + (i + 1) + " " + snippets[s]);
                }
            }
            if (depth <= 0) {
                break;
            }
        }
    }

    private static int braceDelta(String line) {
        int delta = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '{') {
                delta++;
            } else if (c == '}') {
                delta--;
            }
        }
        return delta;
    }

    private static boolean isComment(String trimmed) {
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*");
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
