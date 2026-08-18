# Changelog

All notable changes to AMPER will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- `publishTelemetry` now rate-limits Driver Station lines when Phase 1 is off or AMPER is disabled, so `measurementOnly()` still shows `AMPER.V` and `disabled()` shows `AMPER_DISABLED` (#34).
- Stall suspicion dwell continues across round-robin `SKIPPED` current samples when command, velocity, and carried amps still look jammed (#33). Missing current still does not invent a stall.
- Weak-battery hints use a 2 s voltage window instead of match-long max/min, so one sag can clear after recovery (#35).

### Added

- CI compiles `amper-ftc` and `amper-examples` against official `org.firstinspires.ftc:RobotCore:11.2.0` (`./gradlew compileAgainstFtcSdk`). Default `check` still uses `amper-ftc-stubs` for JVM unit tests (#36).
- Canonical `/AMPER` log model with AdvantageScope table/list CSV, schema sidecar, and desktop WPILOG conversion (WPILib DataLog format 1.0). Robot-side format is AdvantageScope CSV; native WPILOG is not used on the current Control Hub.
- Initial deep audit, 0.1.x roadmap, and priority ledger (`docs/audits/`, `docs/status/`).

### Fixed

- Convention docs now match Java 8 bytecode (FTC SDK 11.2), not ViDAR Java 11.
- Quickstart no longer claims Driver Station `AMPER.V` lines during `init()`.

## [0.1.0-rc.1] - 2026-08-17

### Added

- Multi-module layout: `amper-core`, `amper-ftc`, `amper-examples`, `amper-tools`.
- `AmperFtc.builder(hardwareMap)` FTC integration (deterministic voltage discovery, `DcMotorEx` observation).
- Session lifecycle (`initialize` / `start` / `observe` / `publishTelemetry` / `stop` / `close`).
- Schema-1 bounded CSV, FTC file export to `/sdcard/FIRST/amper/`, desktop analyze tool.
- Configurable sampling / round-robin motor current, stale/skipped validity.
- Validated `PowerPolicy` (finite ordered thresholds, bounded logger).
- Compile-checked example OpModes including hardware characterization (not hardware-validated).
- Phase 2 opt-in foundations (`SlewRateLimiter`, `LocalProtection`, gravity declaration) disabled by default.
- Phase 3 voltage state-machine foundation without output intervention.
- Deterministic replay traces (not hardware validation).
- Issue matrix, install/quickstart, validation templates.

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
