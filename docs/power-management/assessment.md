# Assessment (initial)

Evidence basis: source-backed research in [research.md](research.md), architecture constraints, and the Phase 0 scaffold. **No Allsparks on-robot AMPER dataset exists yet** — hardware timing claims remain provisional.

## Rookie-team worthwhile phases

| Phase | Worth for rookies? | Why |
|-------|--------------------|-----|
| **0** | **Yes** | Teaches real measurements; zero behavior change |
| **1** | **Yes** | Correlates sag with actions; still safe |
| **2** | Selective | Slew/stall timeouts help; enable per-mechanism after graphs |
| **3** | After solid robot | Needs hysteresis tuning + driver trust |
| **4** | Advanced | High value when many mechanisms fight; more integration cost |
| **5–6** | Research first | Need data; false-safe predictions are dangerous |
| **7** | Optional later | Easy to overfit / overtrust |

## Best benefit / complexity

1. **Phase 1 logging + student graphs** — highest teaching ROI.  
2. **Phase 2 drivetrain slew + intake stall timeout** — practical reliability.  
3. **Phase 3** — only after Phase 1 shows clear sag events worth reacting to.

## Wait until mechanically complete

- Elevator/arm gravity policy (Phase 2+/4)
- Priority coordination (Phase 4)
- Any predictive shaping (Phase 6)

## Primarily research

- Effective resistance estimation quality (Phase 5)
- Predictive load shaping vs reactive-only (Phase 6)
- Adaptive per-battery profiles (Phase 7)
- SystemCore capability review

## Standalone vs published artifact

**Remain a standalone public repo** (TeamCode copy or composite include) until Phase 1 is hardware-validated by Allsparks. Revisit Maven/publication after API stability and season SDK compatibility notes exist.

## Can current FTC hardware react quickly enough?

| Phase | Provisional judgment |
|-------|----------------------|
| 0–1 | Yes — sampling/telemetry pace |
| 2 | Yes — command shaping is local and immediate |
| 3 | **Maybe** — depends on voltage freshness vs sag speed; measure Hub latency |
| 4 | Yes for allocation logic; still limited by sensing freshness |
| 5–6 | **Unproven** — prediction helps only if estimate beats reaction delay + confidence gating |

## SystemCore

**Verified:** none claimed in this repository.  
**Possible:** richer telemetry or different brownout margins (**future-hardware possibility** only). AMPER keeps an unimplemented adapter boundary and a blocked milestone/issue until primary documentation exists.
