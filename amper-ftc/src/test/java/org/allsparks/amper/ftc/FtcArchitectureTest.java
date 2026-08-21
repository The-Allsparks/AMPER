package org.allsparks.amper.ftc;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * FTC adapter sources must remain observation-only. Javadoc may mention
 * {@code setPower}; executable calls must not.
 */
class FtcArchitectureTest {

    @Test
    void ftcAdaptersDoNotCallActuatorWrites() throws IOException {
        Path main = ftcMain();
        List<String> hits = new ArrayList<String>();
        try (java.util.stream.Stream<Path> walk = Files.walk(main)) {
            walk.forEach(path -> {
                if (!path.toString().endsWith(".java") || !Files.isRegularFile(path)) {
                    return;
                }
                String text = read(path);
                String[] lines = text.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String trimmed = lines[i].trim();
                    if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                        continue;
                    }
                    if (trimmed.contains(".setPower(") || trimmed.contains(".setVelocity(")) {
                        hits.add(main.relativize(path).toString().replace('\\', '/') + ":" + (i + 1));
                    }
                }
            });
        }
        if (!hits.isEmpty()) {
            fail("amper-ftc must not call actuator writes:\n" + String.join("\n", hits));
        }
    }

    @Test
    void ftcDoesNotDependOnDesktopTools() throws IOException {
        Path main = ftcMain();
        List<String> hits = new ArrayList<String>();
        try (java.util.stream.Stream<Path> walk = Files.walk(main)) {
            walk.forEach(path -> {
                if (!path.toString().endsWith(".java") || !Files.isRegularFile(path)) {
                    return;
                }
                String[] lines = read(path).split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String trimmed = lines[i].trim();
                    if (trimmed.startsWith("import org.allsparks.amper.tools.")) {
                        hits.add(main.relativize(path).toString().replace('\\', '/') + ":" + (i + 1));
                    }
                }
            });
        }
        if (!hits.isEmpty()) {
            fail("amper-ftc imported amper-tools:\n" + String.join("\n", hits));
        }
    }

    private static Path ftcMain() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path cur = cwd;
        for (int i = 0; i < 8 && cur != null; i++) {
            Path candidate = cur.resolve("amper-ftc/src/main/java");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            cur = cur.getParent();
        }
        return cwd.resolve("src/main/java");
    }

    private static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }
}
