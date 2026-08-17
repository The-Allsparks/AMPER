# Changelog

All notable changes to AMPER will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
