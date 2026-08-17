# Examples

These sketches show integration intent. They are **not** full FTC OpModes (no `hardwareMap` dependency in the library build).

## Phase 0 — observe only

1. Construct `RevHubTelemetrySource` with a voltage supplier.
2. Optionally add `RevMotorTelemetry` per motor with current suppliers.
3. Call `AmperSession.observe()` (or `PowerMonitor.update()`) once per loop.
4. Leave all `setPower` / velocity PID calls unchanged.

See [integration.md](../docs/power-management/integration.md).

## Phase 1 — warnings without actuation

```java
PowerPolicy policy = PowerPolicy.builder()
    .featureFlags(AmperFeatureFlags.passiveTelemetry())
    .build();
AmperSession amper = AmperSession.create(policy, hub, motors);
ElectricalObservation obs = amper.observe();
if (amper.driverTelemetry().publishedThisCycle()) {
    // telemetry.addData("AMPER", amper.driverTelemetry().state());
}
```

Do not modify motor outputs when warnings fire. Call `amper.recordMatchSummary()` at the end of the OpMode.

## Later phases

Do not enable from examples until acceptance tests in [phases.md](../docs/power-management/phases.md) pass and maintainers review.
