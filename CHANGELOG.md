# Changelog

All notable changes to AMPER will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Optional `ElectricalObservationSink` on `AmperSession` / `AmperFtc.Builder` (NOOP default). TRACE adapters implement it; AMPER does not import TRACE.
- Contracts input SPI: `AmperSignals`, `InputValuesPowerSource`, `InputValuesMotorTelemetry`, `OvercurrentFollowUp`, and `AmperFtc.Builder.declareInputs` / `readFrom` / `requestThrough` / `watchMotor` / `physicalVoltages`. AMPER declares voltage and motor keys and can observe a published snapshot. Over-current flags are watched every cycle; a trip `requestOnce`s motor current for the next capture. AMPER does **not** depend on `org.allsparks.pulse` and does **not** call `DcMotorEx.getCurrent` on the Drive path. Standalone `controlHubVoltage()` still calls `VoltageSensor.getVoltage()` when no registrar is supplied. AMPER's current-read `SamplingPolicy` is unchanged (`maxCurrentReadsPerLoop == 0` on student presets). Over-current follow-up uses `InputDemand.isRequested` so a key already queued (PULSE cap or OTHER budget) counts as this cycle's one demand. Primitive peek uses `tryGetBoolean` / `tryGetDouble` (no `Sample` allocation).
- `InputValuesPowerSource` reads `getDouble` plus `validity` / `captureTimestampNanos` instead of `InputValues.get()`, so observe does not box a `Sample`.

### Changed

- Student presets (`measurementOnly`, `passiveDefaults`, `localProtectionAllowed`, `disabled`) use `SamplingPolicy.hubCurrentPreferred()`: hub voltage every loop, **zero** per-motor current reads. Drive must not call `DcMotorEx.getCurrent` on the four wheels. Characterization OpModes may still use `SamplingPolicy.recommended()` (one motor per loop) or `everyLoop()`.
- REV hub-level battery current remains `UNSUPPORTED` until a verified SDK API is wired. Do not fake total current by summing motor channels.

## [0.1.0-rc.2] - 2026-08-23

Software since `v0.1.0-rc.1`. Hardware validation is still **not yet run**.

### Added

- Architecture tests, desktop observe budget at 4000-sample logger capacity, Spotless, CodeQL required check.
- Private vulnerability reporting (`SECURITY.md`); GitHub secret scanning and Dependabot security updates enabled on the repo.
- Control Hub session runbook: `docs/validation/hub-session.md` (#6 / remaining #41 cost rows).
- FTC integration software checklist: `docs/status/ftc-integration-checklist.md`.

### Changed

- Gradle wrapper **9.7.0**; GitHub Actions pinned to commit SHAs (checkout v7, setup-java v5, upload-artifact v7, CodeQL v4).
- `PowerPolicy` default sampling is the recommended current cadence.
- Canonical last-event O(1); ring-buffer log overflow; reuse observe-path buffers.
- Driver Station / match summary publishes observe p95 and max.
- Experimental Phase 2–5 types marked non-student API; `amper-core` `-Werror` on unchecked/deprecation.
- Phase 0/1 software issues #1–#5 closed; Hub evidence lives only on #6.

### Safety

- Motor-output intervention remains default-off. Do not enable Phase 2 from desktop CI.

## [0.1.0-rc.1] - 2026-08-20

### Added

- Multi-module layout: `amper-core`, `amper-ftc`, `amper-examples`, `amper-tools`.
- `AmperFtc.builder(hardwareMap)` FTC integration (deterministic voltage discovery, `DcMotorEx` observation).
- Session lifecycle (`initialize` / `start` / `observe` / `publishTelemetry` / `stop` / `close`).
- Schema-1 bounded CSV, FTC file export to `/sdcard/FIRST/amper/`, desktop analyze tool.
- Configurable sampling / round-robin motor current, stale/skipped validity.
- Validated `PowerPolicy` (finite ordered thresholds, bounded logger).
- Compile-checked example OpModes including hardware characterization (not hardware-validated).
- Phase 2 dual opt-in for team pickup: `AmperPolicies.localProtectionAllowed()`, `AmperSession.localProtection` / `constrain`, session-flag kill switch on `LocalProtection.fromPolicy` (#26), and `AmperLocalProtectionOpMode` example. Still default-off; AMPER does not auto-wrap motors.
- Phase 2 opt-in foundations (`SlewRateLimiter`, `LocalProtection`, gravity declaration) disabled by default.
- Phase 3 voltage state-machine foundation without output intervention.
- Deterministic replay traces (not hardware validation).
- Sibling electrical contracts for MIMIC, BEACON, HELM, and TRACE: `docs/integration/sibling-contracts.md` (#44).
- CI compiles `amper-ftc` and `amper-examples` against official `org.firstinspires.ftc:RobotCore:11.2.0` (`./gradlew compileAgainstFtcSdk`). Default `check` still uses `amper-ftc-stubs` for JVM unit tests (#36).
- Canonical `/AMPER` log model with AdvantageScope table/list CSV, schema sidecar, and desktop WPILOG conversion (WPILib DataLog format 1.0). Robot-side format is AdvantageScope CSV; native WPILOG is not used on the current Control Hub.
- Initial deep audit, 0.1.x roadmap, and priority ledger (`docs/audits/`, `docs/status/`).
- Issue matrix, install/quickstart, validation templates.

### Changed

- `observe()` before `start()` is an init/init_loop probe only: live sensing and Driver Station hints, but no match CSV rows or match summary samples. `start()` clears init probes and begins match accounting (#28).
- `assembleReleaseArtifacts` builds only `amper-core` and `amper-ftc`. `amper-tools` stays a desktop converter (`check` / WPILOG fixture). `check` fails if stubs or tools appear on the `amper-ftc` robot classpath (#43).
- Priority ledger and 0.1.x roadmap list FTC integration epic #41 as the first readiness priority; tracking epic remains #24. Phase 2–7 stay gated. Desktop tests are not Control Hub validation (#42).

### Fixed

- `publishTelemetry` now rate-limits Driver Station lines when Phase 1 is off or AMPER is disabled, so `measurementOnly()` still shows `AMPER.V` and `disabled()` shows `AMPER_DISABLED` (#34).
- Stall suspicion dwell continues across round-robin `SKIPPED` current samples when command, velocity, and carried amps still look jammed (#33). Missing current still does not invent a stall.
- Weak-battery hints use a 2 s voltage window instead of match-long max/min, so one sag can clear after recovery (#35).
- Convention docs now match Java 8 bytecode (FTC SDK 11.2), not ViDAR Java 11.
- Quickstart no longer claims Driver Station `AMPER.V` lines during `init()`.

### Safety

- All motor-output intervention features remain disabled by default.
- Phase 0/1 adapters and tests fail if `setPower` / `setVelocity` are invoked.
- Hardware validation remains **not yet run**.

## [0.1.0-SNAPSHOT] - 2026-08-17

### Added

- Initial public repository scaffold for The Allsparks FTC Team 36117.
- Phase 0–1 implemented: measurement, command logging, start/stop events, rate-limited driver warnings, match summaries, loop-overhead stats.
- Source-backed power-management research, architecture, phased roadmap, and student documentation.
- CI for compile, unit tests, and relative documentation link checks.

### Safety

- All motor-output intervention features remain disabled by default.
