# AMPER hardware result template

Copy this file to `docs/validation/results/YYYY-MM-DD-robot.md` after a real run.

```yaml
status: not-yet-run # change to completed
date:
robot:
control_hub_firmware:
expansion_hub: none | present
ftc_sdk:
amper_version: 0.1.0-rc.1
battery_healthy_id:
battery_weaker_id:
notes: >
  Do not invent numbers. Leave cells blank if not measured.
```

| Metric | Disabled | Voltage only | 1 current / loop | N current / loop | N round-robin |
|--------|----------|--------------|------------------|------------------|---------------|
| Mean OpMode loop us |  |  |  |  |  |
| Max OpMode loop us |  |  |  |  |  |
| Mean AMPER update us | n/a |  |  |  |  |
| Max AMPER update us | n/a |  |  |  |  |
| Telemetry visible |  |  |  |  |  |
| Motor behavior unchanged (yes/no) |  |  |  |  |  |

Additional:

- Recommended round-robin cadence:
- Data failure behavior:
- Healthy vs weaker sag notes:
- Multi-hub source IDs:
- Log file path:
- CSV schema version:
