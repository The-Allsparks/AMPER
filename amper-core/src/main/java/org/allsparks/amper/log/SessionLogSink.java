package org.allsparks.amper.log;

import java.io.IOException;

/**
 * Destination for a completed session CSV. Implementations must not be invoked
 * from the robot control loop except as an explicit post-match flush.
 */
public interface SessionLogSink {
    /**
     * Persist CSV text. Callers must already have bounded the in-memory log.
     *
     * @param filename sanitized file name only, no directory components
     * @param csvContents complete CSV document
     */
    void export(String filename, String csvContents) throws IOException;
}
