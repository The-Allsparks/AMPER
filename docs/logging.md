# Logging and export

AMPER keeps a **bounded in-memory canonical log** during the OpMode. File sinks run after the match (`AmperSession.stop()`), not in the control loop. A sink failure increments `sinkFailureCount()` and must not change measurements or policy.

The **student-facing robot export** is an [AdvantageScope](https://docs.advantagescope.org/overview/log-files/)-compatible CSV (table layout). That is the required first-release visualization path.

Internal diagnostic event CSV (`exportCsv()`, schema `amper_csv_schema=1`, timestamps in nanoseconds) remains available for unit tests and `amper-analyze`. Do not treat it as the format to open in AdvantageScope.

## Canonical model

One hardware-independent model (`CanonicalLog` / `/AMPER/...` keys) feeds every sink:

| Sink | When | Module |
|------|------|--------|
| Bounded in-memory `CanonicalLog` | Every `observe()` | `amper-core` |
| Post-match FTC file (AdvantageScope CSV + schema sidecar) | `stop()` / `flushLog()` | `amper-ftc` |
| AdvantageScope CSV encoder (table and list) | Export boundary | `amper-core` |
| Desktop WPILOG converter | Laptop / CI | `amper-tools` (not on the robot) |

Optional / not implemented on the current Control Hub:

- Native WPILOG or WPILib DataLog in the FTC runtime
- SystemCore / WPILib DataLog adapter (wait for authoritative FIRST/REV APIs)
- Live NetworkTables / AdvantageScope stream
- MCAP converter
- CTRE Hoot or REVLOG writers (reference formats only)

See [compatibility.md](compatibility.md) for the native-WPILOG decision.

## AdvantageScope CSV

Official layouts (AdvantageScope log-file docs, accessed **2026-08-17**):

Table (robot default):

```text
Timestamp,/AMPER/System/BusVoltageVolts,/AMPER/Motors/frontLeft/CurrentAmps
0.000000000,12.730000,1.820000
```

List:

```text
Timestamp,Key,Value
0.000000000,/AMPER/System/BusVoltageVolts,12.730000
```

Rules AMPER enforces:

- Header spelling is exactly `Timestamp` (then keys, or `Key,Value` for list).
- External timestamps are **decimal seconds**, `Locale.US`. Internal time stays integer nanoseconds.
- Booleans are `true` / `false`.
- Missing numeric measurements are **empty cells**, never `0` or `NaN`.
- Strings are quoted. Commas inside messages do not break columns.
- A field’s type does not change during a session.

Committed example: [fixtures/amper-advantagescope-table.csv](logging/fixtures/amper-advantagescope-table.csv) with sidecar [fixtures/amper-advantagescope-table.schema.json](logging/fixtures/amper-advantagescope-table.schema.json). List layout: [fixtures/amper-advantagescope-list.csv](logging/fixtures/amper-advantagescope-list.csv).

Student walkthrough: [logging/advantagescope.md](logging/advantagescope.md). Field picking: [logging/field-selection.md](logging/field-selection.md).

## On-robot location

`FtcSessionLogSink` writes to:

1. `/sdcard/FIRST/amper/` when that FIRST folder is writable
2. otherwise `Context.getExternalFilesDir("amper")`

Files:

- `amper-session.csv` — AdvantageScope table
- `amper-session.schema.json` — units, sources, hardware-map name mapping

Filenames are sanitized to `[A-Za-z0-9._-]`.

## How a student retrieves the file

1. Stop the OpMode (export happens in `stop()`).
2. Android Studio **Device File Explorer**: `storage/emulated/0/FIRST/amper/`
3. or `adb pull /sdcard/FIRST/amper/`
4. Copy the `.csv` (and optional `.schema.json`) to a laptop.
5. Open the CSV in AdvantageScope — see [advantagescope.md](logging/advantagescope.md).

## Field hierarchy

Stable keys under `/AMPER`. Configured hub / motor / mechanism names are sanitized so they cannot inject `/`. The sidecar maps FTC hardware-map names to sanitized segments.

At minimum:

- `/AMPER/Metadata/SchemaVersion`
- `/AMPER/Metadata/LibraryVersion`
- `/AMPER/Metadata/SessionId`
- `/AMPER/Metadata/HardwarePlatform`
- `/AMPER/System/BusVoltageVolts`
- `/AMPER/System/FilteredVoltageVolts`
- `/AMPER/System/MinimumVoltageVolts`
- `/AMPER/System/MeasurementValidity`
- `/AMPER/System/PowerState`
- `/AMPER/Hubs/<hub>/VoltageVolts`
- `/AMPER/Hubs/<hub>/SampleAgeSeconds`
- `/AMPER/Motors/<motor>/Command`
- `/AMPER/Motors/<motor>/AppliedCommand`
- `/AMPER/Motors/<motor>/CurrentAmps`
- `/AMPER/Motors/<motor>/VelocityTicksPerSecond`
- `/AMPER/Motors/<motor>/CurrentSampleAgeSeconds`
- `/AMPER/Motors/<motor>/StallSuspected`
- `/AMPER/Mechanisms/<mechanism>/RequestedEffort`
- `/AMPER/Mechanisms/<mechanism>/GrantedEffort`
- `/AMPER/Mechanisms/<mechanism>/Constrained`
- `/AMPER/Performance/UpdateDurationSeconds`
- `/AMPER/Performance/LoopDurationSeconds`
- `/AMPER/Performance/DroppedRecords`
- `/AMPER/Events/Type`
- `/AMPER/Events/Message`

`/AMPER/System/SelectedMotorsCurrentAmps` is the sum of **VALID** currents for motors AMPER is observing. It is **not** robot total current and is **not** named `TotalCurrentAmps`.

Phase 0/1: `AppliedCommand` equals `Command`, and mechanism `GrantedEffort` equals `RequestedEffort` with `Constrained=false` (identity; AMPER does not allocate).

## Units

Public numeric names include the unit (`VoltageVolts`, `CurrentAmps`, `DurationSeconds`, `VelocityTicksPerSecond`). Do not mix milliseconds and seconds on the same field. Encoder velocities stay in ticks until a conversion is explicit (`VelocityRotationsPerSecond`).

## Validity

| Validity | AdvantageScope numeric cell |
|----------|-----------------------------|
| `VALID` | Measured value |
| `SKIPPED` / `STALE` / `MISSING` / `UNSUPPORTED` / `OUT_OF_RANGE` | Empty |

Hub-level **total battery current is not claimed**. FTC does not provide FRC PDH/PDP branch currents, roboRIO brownout state, or TalonFX supply-current limiting.

## WPILOG

Desktop: `./gradlew :amper-tools:generateWpiLogFixture` or

```text
java -cp amper-tools.jar org.allsparks.amper.tools.AmperConvert --to-wpilog amper-session.csv out.wpilog
```

The converter follows the WPILib Data Log File Format 1.0 (allwpilib v2026.2.1 `datalog.adoc`). CI uploads a WPILOG built from the fixture; the binary is not required in git.

## Bounded memory

`PowerPolicy.loggerCapacity()` defaults to 4000 samples. Older rows drop; `/AMPER/Performance/DroppedRecords` counts overwrites.
