# References

Access date: **2026-08-17** unless noted. Prefer primary sources.

| Title | Org / author | URL | Date / rev | Hardware / software gen | Claim supported | Directly FTC? | Limitations |
|-------|--------------|-----|------------|-------------------------|-----------------|---------------|-------------|
| Control Hub Troubleshooting | REV Robotics | https://docs.revrobotics.com/duo-control/troubleshooting-the-control-system/control-hub-troubleshooting | Docs as retrieved 2026-08-17 | REV Duo Control Hub | Brownout-like symptoms (~9 V displayed, DS disconnect) | Yes | Not a full voltage FSM table |
| Integrated Sensors | REV Robotics | https://docs.revrobotics.com/duo-control/control-system-overview/integrated-sensors | Docs as retrieved 2026-08-17 | Control/Expansion Hub | Accessible voltage & currents; servo bus current not accessible | Yes | Some sensors internal-only |
| Duo Control documentation home | REV Robotics | https://docs.revrobotics.com/duo-control/ | Docs as retrieved 2026-08-17 | Duo Control | Canonical REV FTC control docs | Yes | Evolving with product lines |
| FTC Docs | FIRST | https://ftc-docs.firstinspires.org/ | Retrieved 2026-08-17 | FTC | Official FTC documentation portal | Yes | Broad; topic-specific pages vary |
| FtcRobotController | FIRST Tech Challenge | https://github.com/FIRST-Tech-Challenge/FtcRobotController | Ongoing | FTC SDK | Canonical SDK tree for season projects | Yes | Season branches differ |
| RobotCore Javadocs (`VoltageSensor`, `DcMotorEx`) | FIRST / javadoc.io | https://javadoc.io/doc/org.firstinspires.ftc/RobotCore | Artifact versions vary | RobotCore | `getVoltage`, `getCurrent` APIs | Yes | Pin exact Maven version in team notes |
| Using a Voltage Sensor | FIRST FTC Community | https://ftc-community.firstinspires.org/t/using-a-voltage-sensor/464 | 2023-01 | FTC Java | Example Control Hub voltage read | Yes | Forum guidance, not a spec |
| SDK Motors | Game Manual 0 | https://gm0.org/en/latest/docs/software/adv-control-system/sdk-motors.html | Retrieved 2026-08-17 | REV Hub + FTC SDK | PWM average voltage; BRAKE/FLOAT; current not bulk-read | Yes (community) | Community wiki — verify vs SDK |
| Bulk Reads tutorial | Game Manual 0 | https://gm0.org/en/latest/docs/software/tutorials/bulk-reads.html | Retrieved 2026-08-17 | FTC SDK | Bulk-read performance context | Yes (community) | Community wiki |
| roboRIO Brownouts | WPILib | https://docs.wpilib.org/en/stable/docs/software/roborio-info/roborio-brownouts.html | Stable WPILib docs | FRC roboRIO | Staged brownout; power budgeting advice | No (FRC) | Do not copy thresholds blindly |
| Power Distribution Module | WPILib | https://docs.wpilib.org/en/stable/docs/software/can-devices/power-distribution-module.html | Stable WPILib docs | FRC PDP/PDH | Channel current / bus voltage monitoring patterns | No (FRC) | Conceptual transfer only |
| Feedforward | WPILib | https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/feedforward.html | Stable WPILib docs | FRC | Voltage-based mechanism modeling ideas | Partial | Units/controllers differ on FTC |
| TalonFX current limits | CTRE | https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html | Phoenix 6 docs | FRC Talon FX | Firmware current limiting | No | Not REV Hub motor ports |
| AdvantageScope log files (CSV import) | Mechanical Advantage | https://docs.advantagescope.org/overview/log-files/ | Docs as retrieved 2026-08-17 | AdvantageScope | Table/list CSV layouts; timestamps in seconds; true/false; empty missing cells | Visualization (FRC-first, CSV is generic) | CSV export/reimport is documented as lossy vs WPILOG |
| AdvantageScope export | Mechanical Advantage | https://docs.advantagescope.org/overview/log-files/export/ | Docs as retrieved 2026-08-17 | AdvantageScope | CSV table/list, WPILOG, MCAP export | Desktop | Lossy CSV |
| WPILib Data Log File Format 1.0 | WPILib | https://github.com/wpilibsuite/allwpilib/blob/v2026.2.1/wpiutil/doc/datalog.adoc | allwpilib v2026.2.1 | WPILOG | Header `WPILOG` + 0x0100; timestamps in integer microseconds; JSON metadata | Desktop conversion | Not shipped in FTC SDK 11.2 |
| AdvantageKit Logger timestamps | Mechanical Advantage | https://docs.advantagekit.org/javadoc/org/littletonrobotics/junction/Logger.html | Docs as retrieved 2026-08-17 | AdvantageKit | `getTimestamp()` in microseconds; hierarchical output keys | Convention only | AMPER is not an AdvantageKit `/RealOutputs` logger |
| Phoenix 6 signal logging (Hoot) | CTRE | https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/signal-logging.html | Phoenix 6 docs as retrieved 2026-08-17 | FRC Phoenix 6 | Vendor `.hoot` logger | Reference only | Not an AMPER robot-side dependency |
| REVLOG | Mechanical Advantage (via AdvantageScope) | https://docs.advantagescope.org/overview/log-files/ | Docs as retrieved 2026-08-17 | REV StatusLogger | AdvantageScope can open `.revlog` | Reference only | Not an AMPER robot-side dependency |

When citing public team code in future issues, link a **stable commit** and file path, and confirm the snippet implements the claimed behavior.
