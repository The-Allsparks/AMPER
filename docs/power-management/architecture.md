# AMPER architecture

Write and review this architecture **before** enabling active motor control.

## Goals

- Electrical situational awareness complementary to [ViDAR](https://github.com/The-Allsparks/ViDAR) field awareness.
- Phased enablement for rookies: measure → understand → optionally intervene.
- Hardware-independent policy with REV adapters today and a documented SystemCore boundary later.

## Module map

```text
OpMode / scheduler
    │
    └─► AmperSession.observe()   (never writes motors)
            │
            ├─► PowerMonitor  ──► ElectricalObservation (voltage, currents, commands)
            ├─► BatteryEstimator
            ├─► MechanismActivityTracker / StallSuspicionTracker / DriverFeedback
            ├─► PowerEventLogger  (exportable CSV)
            │
            ├─► (Phase 4+) subsystem intent ──► PowerRequest
            │         ▼
            │      PowerCoordinator ◄── PowerPolicy / AmperFeatureFlags
            │         ▼
            │      PowerGrant (advisory until intervention phases enabled)
            │
            └─► subsystem applies motor outputs (subsystems own PID/FF/safety)
```

## `PowerMonitor`

Reads and filters:

- battery/bus voltage;
- motor current where supported;
- commanded effort, velocity, position (via motor telemetry adapters);
- loop timing;
- voltage minima;
- validity / freshness.

**Must not command hardware.**

## `PowerEventLogger`

Records time-correlated raw/filtered voltage, currents, commands, stalls, state transitions, intervention reasons/magnitudes (when those exist), loop duration, and invalid/stale sensing. Export via CSV for offline graphs.

## `BatteryEstimator`

Phase 0/1: basic observations (latest / max / min hints) with explicit non-certain confidence.

Later: optional \(R_{effective}\) style estimates **must** include confidence and remain disabled for actuation until acceptance criteria pass.

## `PowerPolicy`

Central thresholds, hysteresis, priorities, safe minima, ramp limits, recovery timing, feature flags, fallbacks.

## `PowerCoordinator`

Accepts `PowerRequest`s, returns `PowerGrant`s. Does not own hardware. Phase 0–1 always pass through unrestricted grants.

## Subsystem adapters

Translate mechanism intent into requests. Subsystems retain PID, feedforward, motion profiling, limits, gravity compensation, and mechanism safety. AMPER may later constrain allowable output; it must not replace the controller.

## Driver feedback

Rate-limited states: normal, elevated demand, intervention active, severe voltage risk, invalid sensing, suspected weak battery, suspected stall.

## Hardware abstraction

| Interface | Role |
|-----------|------|
| `PowerTelemetrySource` | Hub/bus electrical reads |
| `MotorElectricalTelemetry` | Per-motor electrical + command reads |
| `AmperClock` | Testable time |
| REV adapters | Supplier-wired to SDK on-robot |
| `SystemCoreAdapterBoundary` | Future only — unimplemented |

## Control and safety analysis

| Topic | Risk | Mitigation direction |
|-------|------|----------------------|
| PID integral windup while constrained | Overshoot on release | Freeze/back-calculate integral when grant < request (Phase 3+) |
| Feedforward vs sag | Inconsistent tracking | Prefer voltage-aware FF carefully; avoid increasing demand near critical |
| Velocity vs raw power | Different current profiles | Log both modes separately in characterization |
| Gravity / elevator hold | Dropping load | `gravityCritical` + mechanical counterbalance; never assume depower is safe |
| Counterbalanced elevator (Allsparks) | Reduced but non-zero holding need; imbalance if counterbalance fails | Declare safe minimum effort; keep Phase 2+ holds above minimum; mechanical ratchet/brake preferred |
| Ratchets / counterbalances | Software may under-hold | Mechanical design first |
| BRAKE vs FLOAT | Regeneration / holding differ | Document per mechanism |
| Oscillation | Limit↔recover chatter | Hysteresis + recovery timers |
| Sensor latency / stale Hub data | False safety or false alarm | Stale → `SENSOR_FAULT` / disable intervention |
| SDK exceptions | Missed samples | Adapters catch and mark MISSING |
| Multiple hubs | Different voltage views | Monitor both; define policy source-of-truth |
| Autonomous timing | Slower ramps change trajectories | Characterize before enabling Phase 2+ in auto |
| Driver expectations | Unpredictable soft limits | Telemetry + gradual enablement |
| E-stop / watchdog | Must still stop | Never bypass FTC failsafe paths |

## Recommended robot-loop order

1. Read sensors once (prefer bulk reads where applicable).
2. Update AMPER observations (`PowerMonitor`).
3. Read operator / autonomous intent.
4. Subsystems calculate requested actions.
5. Submit participating `PowerRequest`s (Phase 4+).
6. Evaluate policy / allocate grants.
7. Apply constrained motor outputs (only if intervention enabled).
8. Record applied outputs and events.
9. Publish rate-limited telemetry.

Phase 0/1 stop after steps 1–2 (and optional logging/telemetry); motor outputs remain subsystem-owned and unchanged.
