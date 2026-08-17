# Logging and export

AMPER keeps a **bounded in-memory** log during the OpMode. It does **not** perform large disk writes in the control loop. `AmperSession.stop()` writes one CSV.

## Schema

CSV schema version **1** (`AmperVersion.CSV_SCHEMA_VERSION`). Header comments:

```text
# amper_csv_schema=1
# amper_version=0.1.0-rc.1
# session_id=...
# policy_note=CONSERVATIVE_PLACEHOLDER
# dropped_count=0
# pii_policy=no-personal-information
timestampNanos,type,message,fields
```

`fields` is `key=value;key=value` with locale-independent numbers (`Locale.US`) and CSV escaping for commas/quotes.

Do not put student names, emails, or secrets in `sessionId` / `robotNote`.

## On-robot location

`FtcSessionLogSink` writes to:

1. `/sdcard/FIRST/amper/` when that FIRST folder is writable (conventional Robot Controller location)
2. otherwise `Context.getExternalFilesDir("amper")`

Filenames are sanitized to `[A-Za-z0-9._-]`.

## How a student retrieves the file

1. Stop the OpMode (export happens in `stop()`).
2. Android Studio **Device File Explorer**: `storage/emulated/0/FIRST/amper/`
3. or `adb pull /sdcard/FIRST/amper/`
4. Copy the `.csv` to a laptop.

## How to graph

See [tools/amper-analyze/README.md](../tools/amper-analyze/README.md).

Graph at least:

- raw and filtered voltage vs time
- `sumAbsCmd` and `mechanism_start` / `mechanism_stop` markers
- motor current when validity is `VALID` (ignore `SKIPPED` / `STALE` as if they were fresh)
- AMPER update duration (`loopNs` / `amperNs`)

## Validity

| Validity | Meaning |
|----------|---------|
| `VALID` | Fresh enough and in range |
| `SKIPPED` | Not sampled this loop (round-robin). Numeric value may be from an older capture |
| `STALE` | Age exceeded `staleAfterNanos` |
| `MISSING` | Read failed or NaN. **Not zero** |
| `UNSUPPORTED` | No FTC API for this quantity |
| `OUT_OF_RANGE` | Outside `minValidVolts` / `maxValidVolts` |

Hub-level **total battery current is not claimed**. Per-motor current, hub voltage, and inferred demand (`sumAbsCmd`) are distinct.

## Bounded memory

`PowerPolicy.loggerCapacity()` defaults to 4000 events. Older rows drop; `dropped_count` records how many.
