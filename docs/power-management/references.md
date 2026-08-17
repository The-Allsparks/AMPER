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
| ViDAR repository | The Allsparks | https://github.com/The-Allsparks/ViDAR | 2026 | FTC vision | Team OSS conventions companion | Yes | Different domain (vision) |

When citing public team code in future issues, link a **stable commit** and file path, and confirm the snippet implements the claimed behavior.
