# Examples

These sketches show integration intent. They are **not** full FTC OpModes (no `hardwareMap` dependency in the library build).

## Phase 0 — observe only

1. Construct `RevHubTelemetrySource` with a voltage supplier.
2. Optionally add `RevMotorTelemetry` per motor with current suppliers.
3. Call `PowerMonitor.update()` once per loop.
4. Log with `PowerEventLogger.recordObservation`.
5. Leave all `setPower` / velocity PID calls unchanged.

See [integration](../docs/power-management/integration.md).

## Phase 1 — warnings without actuation

Use filtered voltage and match minimums for Driver Station messages. Do not modify outputs when warnings fire.

## Later phases

Do not enable from examples until acceptance tests in [phases.md](../docs/power-management/phases.md) pass and maintainers review.
