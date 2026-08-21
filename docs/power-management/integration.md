# Integration contract

## Goals

- Phase 0/1 usable in a real FTC TeamCode project without hand-built suppliers.
- Clear loop order for iterative OpModes, LinearOpModes, and command-based styles.

Install: [docs/install.md](../install.md). Five-minute setup: [docs/quickstart.md](../quickstart.md).

## Core types

- `org.allsparks.amper.AmperSession` (core lifecycle)
- `org.allsparks.amper.ftc.AmperFtc` (HardwareMap builder)
- `org.allsparks.amper.policy.AmperPolicies` / `PowerPolicy`
- `org.allsparks.amper.measure.PowerMonitor`

Effort fields use dimensionless \(\approx [-1, 1]\) unless your team documents otherwise in policy.

`estimatedCurrentAmps` / motor amps may be `NaN` when unknown — do not invent currents. Hub **total battery current** is reported `UNSUPPORTED` unless a verified FTC API exists.

## Lifecycle

| When | Call |
|------|------|
| Construction | `AmperFtc.builder(hardwareMap)...build()` in `init` |
| Initialization | `amper.initialize()` (optional probe; safe to repeat) |
| Match start | `amper.start()` (resets match stats; **required** before match CSV / summaries) |
| Init / init_loop probe | Optional `amper.observe()` for live voltage on the Driver Station; does **not** start match accounting |
| Each control loop (match) | **one** `amper.observe()` after `start()` (alias `update()`) |
| Driver Station | `amper.publishTelemetry(AmperFtc.telemetrySink(telemetry))` (rate-limited) |
| Match end | `amper.recordMatchSummary()` is included in `stop()` |
| Stop | `amper.stop()` then `amper.close()` (flush CSV) |

Before `start()`, `observe()` works as an init/init_loop **probe**: live sensing and optional Driver Station hints, but no match rows in the AdvantageScope CSV and no match summary samples. `start()` clears any init probes and begins match accounting. After `close()`, `observe()` throws. A second `observe()` inside the duplicate window (~1 ms) returns the previous sample and logs `DUPLICATE_OBSERVE`.

Sibling electrical contracts (MIMIC, BEACON, HELM, TRACE): [docs/integration/sibling-contracts.md](../integration/sibling-contracts.md).

AMPER does not require a process-global singleton. One session per OpMode is the supported model.

## Iterative OpMode

```java
private AmperSession amper;

@Override
public void init() {
    DcMotorEx frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
    amper = AmperFtc.builder(hardwareMap)
            .controlHubVoltage()
            .observeMotor("frontLeft", frontLeft)
            .policy(AmperPolicies.passiveDefaults())
            .exportFilename("amper-session.csv")
            .build();
    amper.initialize();
}

@Override
public void start() {
    amper.start();
}

@Override
public void loop() {
    amper.observe();
    // Existing subsystem code unchanged — still sets motor powers directly.
    amper.publishTelemetry(AmperFtc.telemetrySink(telemetry));
}

@Override
public void stop() {
    amper.stop();
    amper.close();
}
```

Voltage discovery is deterministic: `controlHubVoltage()` requires exactly one configured sensor whose name contains `Control Hub`. Expansion hubs need an explicit configured name. AMPER will not silently pick `iterator().next()`.

## LinearOpMode

Same session calls inside `runOpMode()`: `initialize` → `waitForStart` → `start` → loop `observe` while `opModeIsActive()` → `stop`. See `AmperLinearOpMode`.

## Command-based / custom schedulers

1. Construct the session in subsystem/OpMode init (not Application `onCreate`).
2. Schedule a high-priority runnable that calls `amper.update()` once per loop.
3. Publish telemetry from the same loop when `driverTelemetry().publishedThisCycle()` is true, or call `publishTelemetry`.
4. Only if a later intervention phase is enabled: allocate grants, then apply. Phase 0/1 never apply.

## Sampling cadence

`AmperPolicies.passiveDefaults()` and `PowerPolicy.defaults()` use `SamplingPolicy.recommended()`: voltage every loop, **at most one motor current read per loop**, round-robin. Characterization can use `SamplingPolicy.everyLoop()`. Skipped currents are `SKIPPED` or `STALE`, never labeled fresh `VALID`.

## Multi-hub

```java
AmperFtc.builder(hardwareMap)
    .controlHubVoltage()
    .expansionHubVoltage("Expansion Hub 1")
    .policySourceIndex(0)
    .build();
```

Policy filters/warnings use the selected source. All labeled voltages appear in the observation and CSV.

## Phase 2 opt-in

Do not wrap every motor. Dual gate:

1. Open the session flag with `AmperPolicies.localProtectionAllowed()` (or set `phase2LocalProtection` on your policy).
2. Build per-subsystem protection with `amper.localProtection(true)` / `LocalProtection.fromPolicy(policy, true)` and apply `ConstrainedCommand.allowed()` **in that subsystem**.

If either gate is off, commands stay identity. Prefer session helpers so the feature flag remains a kill switch; raw `LocalProtection.builder()` without session flags is not kill-switched.

Gravity-critical mechanisms must `GravityHoldPolicy.declare(id, minAbsEffort)`. Automatic recovery stays off. Copy [`AmperLocalProtectionOpMode.java`](../../amper-examples/src/main/java/org/allsparks/amper/examples/AmperLocalProtectionOpMode.java). Experimental until [hardware validation](../validation/STATUS.md) exists.

## Disable

- `AmperPolicies.disabled()` — no hardware reads
- `AmperPolicies.measurementOnly()` — Phase 0, no Phase 1 warnings
- `AmperPolicies.passiveDefaults()` — Phase 0+1, still no motor writes (Phase 2 session gate closed)
- `AmperPolicies.localProtectionAllowed()` — Phase 0+1 + Phase 2 session gate open; still no auto motor wrap

## Copying examples

Compile-checked sources: [`amper-examples`](../../amper-examples). Copy into TeamCode; remove `@Disabled`. Do not copy `amper-ftc-stubs`.
