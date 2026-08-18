package org.allsparks.amper.telemetry;

/**
 * Destination for rate-limited driver telemetry. FTC adapter wraps
 * {@code org.firstinspires.ftc.robotcore.external.Telemetry}.
 */
public interface TelemetrySink {
    void addData(String caption, Object value);

    void update();
}
