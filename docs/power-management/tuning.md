# Tuning

## Phase 0

| Parameter | Default (policy) | Notes |
|-----------|------------------|-------|
| `voltageFilterAlpha` | 0.35 | Higher = less smoothing |
| `staleAfterNanos` | 100 ms | Mark STALE beyond this age |
| `minValidVolts` / `maxValidVolts` | 5 / 16 | Out-of-range guard |
| logger capacity | team choice | Bound memory |

## Phase 1 (warnings only)

| Parameter | Default | Notes |
|-----------|---------|-------|
| `mechanismStartEffort` / `mechanismStopEffort` | 0.10 / 0.05 | Start/stop hysteresis on \|command\| |
| `stallCurrentAmps` / `stallVelocityTicksPerSecond` / `stallDwellNanos` | 8 A / 50 ticks/s / 150 ms | Placeholders — tune per motor |
| `weakBatterySagVolts` | 1.5 V | Suspected weak pack hint |
| `telemetryMinPeriodNanos` | 100 ms | Driver-state publish rate limit |
| `loggerCapacity` | 4000 | Drop-oldest ring |

These thresholds are **not** validated on Allsparks hardware yet.

## Later phases (placeholders — do not treat as validated)

| Parameter | Placeholder | Enable only after |
|-----------|-------------|-------------------|
| watch / limiting / critical voltages | 11.0 / 10.5 / 9.5 V | Team characterization vs DS symptoms |
| recovery voltage + hold | 11.2 V / 250 ms | Anti-chatter tests |

## Feature flags

Defaults: Phase 0 on; Phase 1 off until you call `AmperPolicies.passiveDefaults()` or `AmperFeatureFlags.passiveTelemetry()`; all intervention flags off; Phase 5 shadow-only.

Voltage watch/limiting/critical numbers are `ThresholdProvenance.CONSERVATIVE_PLACEHOLDER` until your team marks `TEAM_TUNED` or `HARDWARE_VALIDATED`.

Never ship competition code with intervention flags enabled “just to try.”
