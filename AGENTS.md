# Agent and contributor engineering rules

This file is the short contract for humans and coding agents working in AMPER.

AMPER is production software for FTC robots. Features may work while still being poorly structured. Do not ask only “does it work?” Ask whether the change is organized, testable, measurable, and hard to accidentally degrade.

## Commands

```powershell
.\gradlew.bat check
.\gradlew.bat compileAgainstFtcSdk
.\gradlew.bat spotlessApply
.\gradlew.bat javadocAll assembleReleaseArtifacts
```

`check` compiles modules, runs unit tests (including architecture and desktop performance-budget tests), compiles examples, asserts robot-facing artifacts, and runs `spotlessCheck`. `compileAgainstFtcSdk` compiles adapters against official RobotCore 11.2.0.

Format with `.\gradlew.bat spotlessApply` (Palantir Java Format, 4-space). Do not mix format-only noise into behavior PRs; `amper-ftc-stubs` is excluded so SDK-shaped stubs stay as-is. Explicit types are fine; do not require Java 9+ APIs in `amper-core` / `amper-ftc`.

## Module responsibilities

| Module | May depend on | Must not |
|--------|----------------|----------|
| `amper-core` | Pure Java 8 | FTC SDK, Android, `amper-ftc`, `amper-tools` |
| `amper-ftc` | `amper-core` + FTC types | `amper-tools`, actuator writes (`setPower` / `setVelocity`) |
| `amper-examples` | `amper-ftc` | Desktop tools on the robot path |
| `amper-tools` | `amper-core` | Robot runtime, FTC SDK |
| `amper-ftc-stubs` | none | Publication, TeamCode |

Architecture tests enforce this. See [docs/architecture/quality-standards.md](docs/architecture/quality-standards.md).

## Allowed dependency direction (`amper-core`)

```text
clock, filter
    ↑
measure  ←  policy (thresholds / sampling config only)
    ↑
battery, log, telemetry, adapters.rev
    ↑
AmperSession (composition root)
    ↑
protect / coord / predict   (experimental; not on the Phase 0/1 observe write path)

sim may depend on production packages.
Production packages must not depend on sim.
```

Do not introduce a DI framework. Pass clocks, policies, and adapters explicitly.

## Public API

Preserve `AmperFtc.builder`, `AmperSession` lifecycle (`initialize` / `start` / `observe` / `publishTelemetry` / `stop`), `AmperPolicies`, and canonical `/AMPER` log keys unless the PR is an explicit breaking SemVer change.

Experimental types (`LocalProtection`, `VoltageStateMachine`, `PowerCoordinator`, `ShadowSagPredictor`) may exist, but must stay default-off and must not be wired into Phase 0/1 motor writes.

## Tests expected for new work

- Behavior change: unit test in the same module.
- New package import across a boundary: architecture test must still pass.
- New hardware adapter: observation-only; spy/fail if outputs are written.
- New logging field: type-stability test / schema sidecar, no silent type changes.
- Hot-path change (`observe`, `PowerMonitor.update`, log append): do not add `Thread.sleep`, filesystem I/O, networking, extra threads, or Java streams.

Desktop tests are **not** Control Hub validation.

## Performance expectations

The robot control loop calls `AmperSession.observe()` once per cycle.

- Do not allocate unbounded structures. Logger capacity must remain bounded (tested).
- Prefer reused buffers over per-loop `ArrayList` / `LinkedHashMap` / `String.format` on the observe path. Existing debt is tracked in GitHub issues; do not add more of it.
- Current sampling: `PowerPolicy.defaults()` and student presets (`AmperPolicies.passiveDefaults()` and friends) must keep `maxCurrentReadsPerLoop() == 1`. Characterization may use `SamplingPolicy.everyLoop()`.
- CSV export belongs in `stop()`, not in `observe()`.
- Do not add worker threads for logging or hardware. FTC SDK hardware calls stay on the OpMode thread.
- CI performance tests use **generous desktop ceilings** and relative slowdown limits. They are not Hub SLAs. Hub numbers remain issue #6.

`LoopOverheadStats` already tracks min/mean/max and p50/p95/p99-style window percentiles. Prefer publishing those over inventing a second timing system.

## Dependencies

- Library bytecode: Java 8. CI JDK: Temurin 17. Gradle wrapper: 9.7.0. Do not merge unanalyzed JUnit 6 majors.
- `amper-core` JavaCompile uses `-Werror` for `-Xlint:unchecked` and `-Xlint:deprecation`. Do not add those warnings. `compileAgainstFtcSdk` stays without `-Werror`.
- Do not merge unanalyzed JUnit 6 majors.
- New Maven coordinates need a reason, Android/FTC compatibility note, and a test.
- ArchUnit, Spotless, PMD, and SpotBugs were evaluated and **not** added: they are either redundant with source architecture tests, or would fail the whole tree without a dedicated formatting/baseline PR.

## Safety

Phase 0/1 must not call `setPower` or `setVelocity`. Missing sensors are `MISSING` / `UNSUPPORTED`, never invented zeros. Do not enable Phase 2+ actuation in a PR without maintainer review, hardware evidence (#6), and documented acceptance tests.

## Documentation

Update docs when behavior, maturity labels, or module boundaries change. Distinguish verified fact, engineering inference, and untested hypothesis. Full standards: [docs/architecture/quality-standards.md](docs/architecture/quality-standards.md).
