package org.allsparks.amper.ftc;

import org.allsparks.amper.telemetry.TelemetrySink;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/** FTC Driver Station telemetry adapter. */
public final class FtcTelemetrySink implements TelemetrySink {
    private final Telemetry telemetry;

    public FtcTelemetrySink(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public void addData(String caption, Object value) {
        if (telemetry != null) {
            telemetry.addData(caption, value);
        }
    }

    @Override
    public void update() {
        if (telemetry != null) {
            telemetry.update();
        }
    }
}
