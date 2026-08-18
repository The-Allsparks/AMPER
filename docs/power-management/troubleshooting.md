# Troubleshooting

| Symptom | Likely cause | AMPER / electrical action |
|---------|--------------|---------------------------|
| DS disconnect when mechanism starts | Sag / connector / weak pack | Fix hardware first; Phase 0 log correlation |
| Voltage telemetry jumps wildly | Loose XT30, bad sensor path | Inspect connectors (REV XT30 guidance) |
| `UNSUPPORTED` motor current | Adapter not wired / device lacks current | Degrade cleanly; do not fake amps |
| Init: expected exactly one Control Hub voltage sensor | Name does not contain `Control Hub`, or several match | Pass `.voltageSensor("label", configuredName)` or rename the device |
| `SKIPPED` current every other loop | Round-robin cadence | Expected with `SamplingPolicy.recommended()`; not a fresh `VALID` sample. Stall dwell still uses carried amps until `STALE`. |
| `STALE` / `MISSING` | Hub comms / exception in adapter | Disable intervention; debug SDK path |
| CSV not on the laptop | `stop()` not called, or looking in the wrong folder | Use `/sdcard/FIRST/amper/` or app `files/amper`; see [logging.md](../logging.md) |
| Loop time regresses | Too many `getCurrent` calls | Reduce poll rate; stagger reads |
| Motors “soft” unexpectedly | Intervention flag enabled | Turn flags off; verify policy |
| Elevator drops when “saving power” | Gravity hold reduced | Raise safe minimum; use mechanical hold |

Software will not fix crushed XT30 pins, undersized wire, or a depleted pack.
