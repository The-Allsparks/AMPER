package org.allsparks.amper.arch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Locates Gradle source trees from unit-test working directories. */
final class SourceScan {
    private SourceScan() {
    }

    static Path repoRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path cur = cwd;
        for (int i = 0; i < 8 && cur != null; i++) {
            if (Files.isRegularFile(cur.resolve("settings.gradle"))) {
                return cur;
            }
            cur = cur.getParent();
        }
        return cwd;
    }

    static Path coreMain() {
        return repoRoot().resolve("amper-core/src/main/java");
    }

    static Path ftcMain() {
        return repoRoot().resolve("amper-ftc/src/main/java");
    }

    static Path examplesMain() {
        return repoRoot().resolve("amper-examples/src/main/java");
    }

    static List<Path> javaFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<Path>();
        if (!Files.isDirectory(root)) {
            return files;
        }
        try (java.util.stream.Stream<Path> walk = Files.walk(root)) {
            walk.forEach(path -> {
                if (path.toString().endsWith(".java") && Files.isRegularFile(path)) {
                    files.add(path);
                }
            });
        }
        return files;
    }

    static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    static String packageOf(Path javaFile, Path sourceRoot) {
        Path relative = sourceRoot.relativize(javaFile).getParent();
        if (relative == null) {
            return "";
        }
        return relative.toString().replace('\\', '/').replace('/', '.');
    }

    static String amperChildPackage(String fullPackage) {
        String prefix = "org.allsparks.amper.";
        if (!fullPackage.startsWith(prefix)) {
            return fullPackage.equals("org.allsparks.amper") ? "" : fullPackage;
        }
        String rest = fullPackage.substring(prefix.length());
        int dot = rest.indexOf('.');
        if (dot < 0) {
            return rest;
        }
        return rest.substring(0, dot);
    }
}
