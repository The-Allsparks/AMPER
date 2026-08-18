package org.allsparks.amper.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Desktop / test sink that writes UTF-8 CSV into a directory. Not an FTC
 * storage policy by itself; the FTC module chooses a legal Control Hub path.
 */
public final class FileSessionLogSink implements SessionLogSink {
    private final File directory;

    public FileSessionLogSink(File directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
    }

    @Override
    public void export(String filename, String csvContents) throws IOException {
        String safe = CsvFormat.sanitizeLeaf(filename);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("unable to create log directory: " + directory.getAbsolutePath());
        }
        File target = new File(directory, safe);
        FileOutputStream out = new FileOutputStream(target);
        try {
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            try {
                writer.write(csvContents == null ? "" : csvContents);
            } finally {
                writer.close();
            }
        } finally {
            out.close();
        }
    }

    public File directory() {
        return directory;
    }
}
