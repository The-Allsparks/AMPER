# Compatibility

Accessed **2026-08-17**.

| AMPER | FTC SDK | Java (library) | Java (TeamCode compileOptions) | Status |
|-------|---------|----------------|--------------------------------|--------|
| `0.1.0-rc.1` | 11.2.0 / 11.2.1 DECODE | 8 bytecode, CI Temurin 17 | 1.8 per SDK `build.common.gradle` | Software prerelease. **Not Control Hub validated** |

Hardware: REV Control Hub and Expansion Hub voltage via FTC `VoltageSensor`. Motor current via `DcMotorEx.getCurrent(CurrentUnit.AMPS)` where the SDK/firmware supports it. Servo-rail current is not exposed (REV integrated-sensors docs).

SystemCore: adapter boundary only. See issue #16. No invented APIs.

## Upgrade

- `0.1.0-SNAPSHOT` scaffold → `0.1.0-rc.1`: new modules `amper-core` / `amper-ftc`. Prefer `AmperFtc.builder(hardwareMap)` instead of manual suppliers. `AmperSession.observe()` remains. CSV gains a schema-1 header; field names such as `rawV` and `m0Cmd` are kept.
