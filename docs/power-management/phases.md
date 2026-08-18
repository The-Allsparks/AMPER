# Phased implementation

Every phase must be independently feature-flagged, testable, observable, reversible, fail-safe on missing measurements, and **disabled by default** until acceptance tests pass.

| Phase | Name | Motor intervention | Status in `0.1.0-rc.1` |
|-------|------|--------------------|-------------------------|
| 0 | Measurement validation | No | **Software implemented and unit-tested. Not Control Hub validated.** |
| 1 | Passive instrumentation | No | **Software implemented** (enable `AmperPolicies.passiveDefaults()`). Not hardware-validated |
| 2 | Independent subsystem protection | Optional local, opt-in | Experimental foundations; **disabled by default** |
| 3 | Reactive voltage protection | Yes if enabled | State-machine foundation only; intervention off |
| 4 | Priority-based coordination | Yes | API stub only |
| 5 | Predictive sag estimation | Shadow only first | Shadow recorder only; no ML |
| 6 | Predictive load shaping | Yes | Not implemented |
| 7 | Adaptive modeling | Experimental | Not implemented |

---

## Phase 0 — Measurement validation

**Teach:** What can the robot actually measure?

Implement: hardware-independent interfaces; REV adapters; voltage/current sampling; timestamps; loop duration; stale/invalid detection; validation procedures.

**Acceptance**

- Measurements tested under known conditions
- Loop overhead measured
- Unsupported sensing degrades cleanly
- Motor behavior unchanged

## Phase 1 — Passive instrumentation

**Teach:** What causes robot voltage to fall?

Filtering, minima, command/current logging, start/stop events, driver warnings, match summaries, exportable logs — **warnings must not modify outputs**.

**Enable:** `AmperPolicies.passiveDefaults()` or `AmperFeatureFlags.passiveTelemetry()` on `PowerPolicy`. Defaults remain Phase 1 off.

**What it observes:** bus voltage, optional currents, commanded effort, loop duration.

**What it controls:** nothing. Driver Station lines are advisory.

**What it cannot solve:** bad batteries, loose XT30, stalls caused by mechanical binding (it can only *flag* suspected stalls).

**Graph:** filtered voltage vs time, overlaid with `mechanism_start` / `mechanism_stop` and `sumAbsCmd`.

**Incorrect behavior:** warnings that coincide with `setPower` changes you did not write — that would be a bug; file it.

**Evidence before Phase 2:** at least one practice session CSV where a known mechanism start lines up with sag, and loop time remains acceptable.

### Exercise

1. Enable Phase 1 on a wheels-off robot.
2. Run intake, then drive, then both.
3. Export CSV and circle the largest voltage drop.
4. Write one sentence: which command started just before the drop?

## Phase 2 — Independent subsystem protection

**Teach:** How can one subsystem behave responsibly?

Optional slew limits, accel ramps, stall/jam detection, intake recovery, output caps, timeouts, careful voltage-normalized commands.

**Enable:** subsystem constructs `LocalProtection.builder().enabled(true)...` and applies `ConstrainedCommand` itself. Never auto-wrap every FTC motor. Gravity-critical mechanisms must `GravityHoldPolicy.declare`. Automatic recovery defaults off.

**Disable:** omit LocalProtection or `LocalProtection.disabled()` — requested command is unchanged.

Explain PWM (Hub) vs ramp limiting vs higher-level time slicing — do not market PWM as a novel AMPER invention.

**Evidence required to advance:** Control Hub characterization of slew/stall on the actual mechanism. Software tests are not that evidence.

## Phase 3 — Reactive voltage protection

**Teach:** How does the robot respond when voltage is already falling?

State machine: `NORMAL`, `WATCH`, `LIMITING`, `CRITICAL`, `RECOVERY`, `SENSOR_FAULT` with hysteresis.

## Phase 4 — Priority-based coordination

**Teach:** How does the robot decide which work matters most right now?

Request/grant API with state-dependent priorities. Suggested order: survival → safety/gravity hold → minimum mobility → scoring → normal drive accel → auxiliary.

## Phase 5 — Predictive voltage-sag estimation

**Teach:** Can the robot estimate what will happen before starting another load?

Only after real-robot datasets. Model is an approximation. Shadow mode required before actuation.

## Phase 6 — Predictive load shaping

**Teach:** How can small timing changes prevent a large electrical problem?

Stagger starts, soften noncritical ramps, reserve demand for scoring — optimize driver-perceived continuity.

## Phase 7 — Optional adaptive modeling

**Teach:** How can a model improve without becoming untrustworthy?

Per-battery profiles, health hints, wiring resistance growth — avoid opaque ML unless necessary.

---

## Student checklist before advancing a phase

For every phase, document:

1. Problem solved  
2. Observations  
3. Controls (if any)  
4. Cannot solve  
5. Why the algorithm works  
6. What to graph  
7. How to enable / disable  
8. Incorrect behavior signs  
9. Evidence required to advance  

Short exercises should use recorded robot CSV from `PowerEventLogger`.
