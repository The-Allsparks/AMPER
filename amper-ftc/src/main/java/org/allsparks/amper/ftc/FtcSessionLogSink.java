package org.allsparks.amper.ftc;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import org.allsparks.amper.log.FileSessionLogSink;
import org.allsparks.amper.log.SessionLogSink;

/**
 * Writes AMPER CSV after the match, not in the control loop.
 *
 * <p>Preferred location: {@code /sdcard/FIRST/amper/} (the conventional FIRST
 * folder on the Robot Controller). Fallback: {@code Context#getExternalFilesDir("amper")}.
 *
 * <p>Retrieve with Android Studio Device File Explorer or
 * {@code adb pull /sdcard/FIRST/amper/}.
 */
public final class FtcSessionLogSink implements SessionLogSink {
    public static final String FIRST_AMPER_DIR = "/sdcard/FIRST/amper";

    private final FileSessionLogSink delegate;
    private final File directory;

    public FtcSessionLogSink(Context context) {
        File first = new File(FIRST_AMPER_DIR);
        File chosen;
        if (canUse(first)) {
            chosen = first;
        } else if (context != null && context.getExternalFilesDir("amper") != null) {
            chosen = context.getExternalFilesDir("amper");
        } else {
            chosen = first;
        }
        this.directory = chosen;
        this.delegate = new FileSessionLogSink(chosen);
    }

    public File directory() {
        return directory;
    }

    @Override
    public void export(String filename, String csvContents) throws IOException {
        delegate.export(filename, csvContents);
    }

    private static boolean canUse(File directory) {
        if (directory.exists()) {
            return directory.isDirectory() && directory.canWrite();
        }
        File parent = directory.getParentFile();
        return parent != null && parent.exists() && parent.canWrite();
    }
}
