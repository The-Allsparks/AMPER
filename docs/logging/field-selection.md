# AdvantageScope field-selection example

Use this checklist when a screenshot of AdvantageScope is not available. Sidebar paths are the AMPER table-layout keys.

```text
LineGraph
├── /AMPER/System/BusVoltageVolts              (measured bus voltage)
├── /AMPER/System/FilteredVoltageVolts         (AMPER low-pass)
├── /AMPER/Motors/frontLeft/CurrentAmps        (measured, empty if not VALID)
├── /AMPER/Motors/frontLeft/Command            (observed effort; AMPER does not write motors)
├── /AMPER/Events/Type                         (STATE_TRANSITION, VOLTAGE_WARNING, …)
└── /AMPER/Events/Message                      (mechanism_start, sag warning, …)
```

Optional overlays:

- `/AMPER/System/MinimumVoltageVolts`
- `/AMPER/System/PowerState`
- `/AMPER/Motors/frontLeft/StallSuspected`
- `/AMPER/Mechanisms/frontLeft/RequestedEffort` (Phase 0/1 identity grant)
- `/AMPER/Performance/UpdateDurationSeconds`

Do **not** look for:

- `TotalCurrentAmps` — AMPER logs `/AMPER/System/SelectedMotorsCurrentAmps` instead
- PDH/PDP channel currents
- roboRIO brownout bits
- SystemCore fields that FIRST/REV have not documented

Hardware-map names with spaces (for example `Control Hub`) appear as sanitized segments (`Control_Hub`). The `.schema.json` sidecar lists `hardwareNameMapping`.

A saved AdvantageScope `.json` layout is not committed: the desktop layout schema is not an AMPER stability guarantee. Recreate the LineGraph from this list.
