package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/** Compile-only stub matching FTC {@code OpMode}. */
public abstract class OpMode {
    public HardwareMap hardwareMap = new HardwareMap();
    public Telemetry telemetry = new NoopTelemetry();

    public abstract void init();

    public void init_loop() {
    }

    public void start() {
    }

    public abstract void loop();

    public void stop() {
    }

    private static final class NoopTelemetry implements Telemetry {
        @Override
        public Item addData(String caption, Object value) {
            return new Item() {
            };
        }

        @Override
        public Item addLine(String line) {
            return new Item() {
            };
        }

        @Override
        public void update() {
        }
    }
}
