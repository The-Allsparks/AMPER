package org.allsparks.amper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PassiveArchitectureTest {
    @Test
    void coreSourcesDoNotCallActuatorWrites() throws IOException {
        Path root = findModuleRoot();
        Path main = root.resolve("src/main/java");
        List<String> hits = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(main)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().replace('\\', '/').contains("/sim/"))
                    .forEach(path -> scan(path, hits));
        }
        if (!hits.isEmpty()) {
            fail("amper-core must not call actuator writes:\n" + String.join("\n", hits));
        }
        assertTrue(Files.isRegularFile(main.resolve("org/allsparks/amper/measure/MotorElectricalTelemetry.java")));
    }

    @Test
    void coreDoesNotDependOnFtcOrAndroid() throws IOException {
        Path root = findModuleRoot();
        Path main = root.resolve("src/main/java");
        List<String> hits = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(main)) {
            paths.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String text = read(path);
                String[] lines = text.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.startsWith("import com.qualcomm")
                            || line.startsWith("import org.firstinspires.ftc")
                            || line.startsWith("import android.")) {
                        hits.add(root.relativize(path).toString());
                        break;
                    }
                }
            });
        }
        if (!hits.isEmpty()) {
            fail("amper-core imported FTC/Android types:\n" + String.join("\n", hits));
        }
        assertFalse(hits.size() > 0);
    }

    private static void scan(Path path, List<String> hits) {
        String text = read(path);
        if (text.contains(".setPower(") || text.contains(".setVelocity(")) {
            hits.add(path.toString());
        }
    }

    private static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private static Path findModuleRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        if (Files.exists(cwd.resolve("src/main/java/org/allsparks/amper"))) {
            return cwd;
        }
        Path nested = cwd.resolve("amper-core");
        if (Files.exists(nested.resolve("src/main/java/org/allsparks/amper"))) {
            return nested;
        }
        return cwd;
    }
}
