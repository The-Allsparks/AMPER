# Compatibility

Accessed **2026-08-17**.

| AMPER | FTC SDK | Java (library) | Java (TeamCode compileOptions) | Status |
|-------|---------|----------------|--------------------------------|--------|
| `0.1.0-rc.1` | 11.2.0 / 11.2.1 DECODE | 8 bytecode, CI Temurin 17 | 1.8 per SDK `build.common.gradle` | Software prerelease. **Not Control Hub validated** |

Hardware: REV Control Hub and Expansion Hub voltage via FTC `VoltageSensor`. Motor current via `DcMotorEx.getCurrent(CurrentUnit.AMPS)` where the SDK/firmware supports it. Servo-rail current is not exposed (REV integrated-sensors docs).

## Logging visualization

First-release tool: **AdvantageScope** (CSV table layout). See [logging.md](logging.md) and [logging/advantagescope.md](logging/advantagescope.md).

WPILib DataLog / WPILOG: desktop converter in `amper-tools` using the published format spec ([datalog.adoc](https://github.com/wpilibsuite/allwpilib/blob/v2026.2.1/wpiutil/doc/datalog.adoc) v1.0, accessed 2026-08-17). **Not** an on-robot FTC dependency.

Native WPILOG on the current REV Control Hub is **not** implemented. FTC SDK 11.2.0 does not ship WPILib DataLog. Adding WPILib native/JNI to the FTC runtime was not shown to be Android-compatible with acceptable package size, CPU, or storage behavior. AdvantageScope CSV is the supported robot-side format. A future SystemCore adapter must use an authoritative FIRST/REV logging API and must not change AMPER’s canonical event model.

CTRE Hoot and REVLOG are vendor reference formats only. AMPER does not depend on them on the robot.

FRC PowerDistribution (PDP/PDH) terminology is used only as vocabulary. AMPER does not claim branch-current monitoring, robot total current, or roboRIO brownout state.

SystemCore: adapter boundary only. See issue #16. No invented APIs.

## CI compile

| Path | What it compiles against | Command |
|------|--------------------------|---------|
| Default / unit tests | `amper-ftc-stubs` (JVM, no Android plugin) | `./gradlew check` |
| SDK drift check | Official `org.firstinspires.ftc:RobotCore:11.2.0` AAR from Maven Central | `./gradlew compileAgainstFtcSdk` |

The GitHub Actions `sdk-compile` job runs the SDK path and **fails** if `AmperFtc` / `FtcMotorTelemetry` / example OpModes do not compile against RobotCore 11.2.0. Stubs stay on the default classpath so tests can construct `HardwareMap` without Android. Stubs are **not** published.

No extra Maven repository or credential is required (same coordinates as [FtcRobotController v11.2 `build.dependencies.gradle`](https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v11.2/build.dependencies.gradle)). Emergency skip: `-Pamper.skipFtcSdkCompile=true` (do not use this in CI).

AMPER’s own Gradle wrapper is **9.7.0**. FTC SDK 11.2 TeamCode still uses the SDK wrapper (Gradle 9.1 / AGP 8.13.2). Do not change the TeamCode wrapper because AMPER upgraded. Composite `includeBuild` of AMPER into an SDK 11.2 project therefore runs AMPER’s scripts on Gradle 9.1; keep those scripts 9.1-compatible.

## Upgrade

- `0.1.0-SNAPSHOT` scaffold → `0.1.0-rc.1`: new modules `amper-core` / `amper-ftc`. Prefer `AmperFtc.builder(hardwareMap)` instead of manual suppliers. `AmperSession.observe()` remains. Robot export is AdvantageScope CSV under `/AMPER/...`; `exportCsv()` still returns the internal diagnostic event log.
