# Five-minute passive setup

Goal: see Control Hub voltage on the Driver Station without changing motor behavior.

Prerequisites: AMPER added to TeamCode using [install.md](install.md). Robot configuration contains a voltage sensor whose name includes `Control Hub`.

## 1. Create an OpMode

Copy [`AmperVoltageOnlyOpMode.java`](../amper-examples/src/main/java/org/allsparks/amper/examples/AmperVoltageOnlyOpMode.java) into `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` (or your team package). Remove `@Disabled`.

Or type this iterative OpMode:

```java
AmperSession amper;

@Override
public void init() {
    amper = AmperFtc.builder(hardwareMap)
            .controlHubVoltage()
            .policy(AmperPolicies.passiveDefaults())
            .build();
    amper.initialize();
}

@Override
public void start() {
    amper.start();
}

@Override
public void loop() {
    // Your existing drive/intake code is unchanged.
    amper.observe();
    amper.publishTelemetry(AmperFtc.telemetrySink(telemetry));
}

@Override
public void stop() {
    amper.stop();
    amper.close();
}
```

## 2. Run the OpMode

Call `amper.start()` when the match begins (iterative `start()` or LinearOpMode after `waitForStart()`). AMPER telemetry is published from `loop()` (or your LinearOpMode loop), not from `init()` in the snippet above. After you press Play you should see `AMPER` and `AMPER.V` on the Driver Station.

Optional: call `amper.observe()` during `init_loop` to preview voltage before Play. Those probes are not written to the match CSV.

If `init` fails with "Expected exactly one voltage sensor containing 'Control Hub'", list configured names and pass an explicit name:

```java
.voltageSensor("Control Hub", "Control Hub")
```

## 3. Disable without touching motors

```java
.policy(AmperPolicies.disabled())
```

or leave Phase 1 off with `AmperPolicies.measurementOnly()`. Motor `setPower` / `setVelocity` calls stay in *your* code. AMPER never issues them in Phase 0/1. Keep calling `amper.publishTelemetry(...)` in `loop()` — measurement-only mode still shows `AMPER` and `AMPER.V` on the Driver Station.

## Next

- Add selected motors: [integration.md](power-management/integration.md)
- Export logs: [logging.md](logging.md)
- LinearOpMode: [`AmperLinearOpMode.java`](../amper-examples/src/main/java/org/allsparks/amper/examples/AmperLinearOpMode.java)
