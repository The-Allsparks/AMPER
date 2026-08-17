# Testing

## Unit tests

Covered in the initial scaffold:

- filters (`LowPassFilter`)
- monitor validity / unsupported current
- logger export
- feature-flag defaults
- coordinator pass-through
- relative doc links

Still required as phases land: hysteresis, state transitions, allocation/starvation, confidence gating, recovery ramps, timing edges.

## Simulation tests

`org.allsparks.amper.sim.TraceGenerator` provides deterministic traces (healthy, weak/high-R, noise, missing, stale, stalled intake, simultaneous starts, loop spikes). `CsvReplay` reads schema-1 CSV. **Simulated results are not hardware validation.**

## Robot tests (procedures)

Adult supervision required. Mechanical precautions first.

| Test | Intent | Precautions |
|------|--------|-------------|
| Wheels off ground | Baseline current/voltage vs command | Secure robot; soft enable |
| Single mechanism characterization | Correlate command ↔ sag | Clear pinch points |
| Restrained drivetrain | Controlled load | Never exceed safe restraint |
| Combined loads | Interaction effects | E-stop ready |
| Weak vs healthy battery | Pack comparison | Label packs; do not over-discharge |
| Full-match simulation | Telemetry realism | Monitor temperature / connectors |

**Do not intentionally create an uncontrolled brownout merely to test recovery.**

Phase 0 acceptance emphasizes unchanged motor behavior and measured loop overhead. Use [hardware-test-card.md](../validation/hardware-test-card.md). Status: [STATUS.md](../validation/STATUS.md).
