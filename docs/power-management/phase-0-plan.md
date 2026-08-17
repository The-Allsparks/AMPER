# Phase 0 — exact file-level plan

## Package layout

```text
src/main/java/org/allsparks/amper/
  AmperPhase.java
  AmperFeatureFlags.java
  api/           PowerRequest, PowerGrant, PowerPriority, PowerLimitReason, DriverPowerState
  clock/         AmperClock, SystemNanoClock
  measure/       samples, interfaces, ElectricalObservation, PowerMonitor
  filter/        LowPassFilter, MinTracker
  log/           PowerEvent, PowerEventType, PowerEventLogger
  battery/       BatteryEstimator (+ confidence types)
  policy/        PowerPolicy
  coord/         PowerCoordinator (pass-through only)
  adapters/rev/  RevHubTelemetrySource, RevMotorTelemetry
  adapters/future/ SystemCoreAdapterBoundary
```

## Done in this scaffold

- [x] Interfaces and immutable samples
- [x] `PowerMonitor` without motor writes
- [x] REV supplier adapters
- [x] Logger CSV export foundation
- [x] Policy + feature flags (intervention off)
- [x] Unit tests for filter/monitor/logger/flags
- [x] Docs + CI
- [x] Command snapshots on each observation (`MotorSnapshot`)
- [x] `AmperSession` facade and loop-overhead stats

## Phase 1 library (this follow-up)

- [x] Mechanism start/stop events
- [x] Rate-limited driver warnings (no output changes)
- [x] Match summary export
- [x] Stall *suspicion* logging (not protection)

## Next Phase 0/1 hardware tasks (issues)

1. Wire adapters to live `VoltageSensor` / `DcMotorEx` on Control Hub.
2. Measure loop overhead with 0 / N current polls.
3. Record validation log under wheels-up known commands.
4. Confirm unsupported paths return `UNSUPPORTED`/`MISSING` without throwing into OpMode.

## Explicit non-goals (stop for review)

- Slew limiting that changes outputs
- Voltage state machine
- Priority allocation that reduces effort
- Predictive models acting on motors
