package org.firstinspires.ftc.robotcore.external;

/** Compile-only stub matching FTC Driver Station {@code Telemetry}. */
public interface Telemetry {
    Item addData(String caption, Object value);

    Item addLine(String line);

    void update();

    interface Item {
    }
}
