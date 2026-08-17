# AMPER Phase 0/1 hardware test card

Adult supervision. Wheels-off or restrained as appropriate. **Do not intentionally induce an uncontrolled brownout.**

AMPER OpMode: `AmperCharacterizationOpMode` (copy from `amper-examples`, remove `@Disabled`). Change `mode` in code between runs. Existing teleop must still set motor powers; AMPER must not.

| Step | Mode | What to do | Record |
|------|------|------------|--------|
| 1 | AMPER disabled | Run your normal loop 30 s, wheels off | Baseline loop time (DS or logger) |
| 2 | Voltage only | Same motions | AMPER update us, DS telemetry cadence |
| 3 | Voltage + 1 motor current every loop | Light intake or one drive motor | Current-poll overhead vs step 2 |
| 4 | Voltage + N motors every loop | N = drivetrain motors you care about | Overhead; note if loop feels worse |
| 5 | Voltage + N round-robin (recommended) | Same as 4 | Overhead; pick a cadence that stays acceptable |
| 6 | Failure behavior | Unplug or rename a voltage sensor in a **safe** config test, or cover a missing motor name at init | Init error message is explicit; missing reads are `MISSING`/`UNSUPPORTED`, not 0.0 |
| 7 | Healthy pack | Labeled good battery, practice-like commands | CSV min/max V, sag vs `sumAbsCmd` |
| 8 | Weaker labeled pack | Different pack, same routine, stop before damage | Comparison CSV; do not over-discharge |
| 9 | Multi-hub | If Expansion Hub installed, `AmperMultiHubOpMode` | Both source IDs present and labeled |
| 10 | Motor unchanged | Compare mechanism motion AMPER on vs disabled | No unexpected `setPower` change |
| 11 | Practice match logging | Full `stop()` export | File on `/sdcard/FIRST/amper/`; loop time still acceptable |

Acceptance: **measured data on this robot**, not copied placeholder volts from docs.
