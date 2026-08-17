# Troubleshooting

| Symptom | Likely cause | AMPER / electrical action |
|---------|--------------|---------------------------|
| DS disconnect when mechanism starts | Sag / connector / weak pack | Fix hardware first; Phase 0 log correlation |
| Voltage telemetry jumps wildly | Loose XT30, bad sensor path | Inspect connectors (REV XT30 guidance) |
| `UNSUPPORTED` motor current | Adapter not wired / device lacks current | Degrade cleanly; do not fake amps |
| `STALE` / `MISSING` | Hub comms / exception in supplier | Disable intervention; debug SDK path |
| Loop time regresses | Too many `getCurrent` calls | Reduce poll rate; stagger reads |
| Motors “soft” unexpectedly | Intervention flag enabled | Turn flags off; verify policy |
| Elevator drops when “saving power” | Gravity hold reduced | Raise safe minimum; use mechanical hold |

Software will not fix crushed XT30 pins, undersized wire, or a depleted pack.
