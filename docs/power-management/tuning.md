# Tuning

## Phase 0

| Parameter | Default (policy) | Notes |
|-----------|------------------|-------|
| `voltageFilterAlpha` | 0.35 | Higher = less smoothing |
| `staleAfterNanos` | 100 ms | Mark STALE beyond this age |
| `minValidVolts` / `maxValidVolts` | 5 / 16 | Out-of-range guard |
| logger capacity | team choice | Bound memory |

## Later phases (placeholders — do not treat as validated)

| Parameter | Placeholder | Enable only after |
|-----------|-------------|-------------------|
| watch / limiting / critical voltages | 11.0 / 10.5 / 9.5 V | Team characterization vs DS symptoms |
| recovery voltage + hold | 11.2 V / 250 ms | Anti-chatter tests |

## Feature flags

Defaults: Phase 0 on; everything else off; Phase 5 shadow-only if estimate code exists.

Never ship competition code with intervention flags enabled “just to try.”
