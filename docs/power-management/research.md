# Research: FTC / FRC power management (source-backed)

**Access date for all URLs unless noted:** 2026-08-17  
**Label key:** **VF** verified fact · **MR** measured result (team-pending) · **EI** engineering inference · **UH** untested hypothesis · **FH** future-hardware possibility

This report distinguishes firmware behavior on other platforms from capabilities available through the current FTC SDK and REV hubs. FRC motor-controller features are **not** claimed as current FTC features without evidence.

---

## 1. What “brownout” means on current FTC hardware

### Findings

1. **VF (symptoms, not a single published FTC voltage table):** REV Control Hub troubleshooting documents brownout-like conditions when mechanisms draw enough power to strain the battery. Indicators include Driver Station power errors, disconnect sounds, displayed voltage around **9 V or lower while running code**, and motors running slower than commanded.[^rev-ch-trouble]
2. **EI:** In FTC practice, “brownout” usually means the robot controller / radio path becomes unreliable or the Hub I/O path fails under sag—not necessarily the same staged PWM-disable scheme as the FRC roboRIO.[^rev-ch-trouble][^wpilib-brownout]
3. **VF (FRC contrast):** The NI roboRIO implements a **staged** brownout scheme with documented voltage triggers (for example Stage 2 output disable near 6.3 V on roboRIO 1.0, with recovery hysteresis to 7.5 V).[^wpilib-brownout]
4. **EI:** Applying FRC brownout trigger voltages directly as FTC Hub thresholds is unjustified; FTC teams should characterize their own Hub/DS failure symptoms under load.

### Label summary

| Claim | Label |
|-------|-------|
| REV documents brownout-like DS disconnect / low displayed voltage symptoms | VF |
| FTC has an identical published staged brownout FSM to roboRIO | Not supported |
| Software can prevent all Hub resets | False / UH if claimed |

---

## 2. Existing hardware and firmware protections

### FTC / REV

| Protection | Evidence | Applies to FTC? | Notes |
|------------|----------|-----------------|-------|
| Battery voltage monitoring on Control/Expansion Hub | REV integrated sensors list Battery Voltage as accessible[^rev-sensors] | Yes | Via SDK `VoltageSensor` |
| Per-motor channel current monitoring | REV lists per-motor current as accessible[^rev-sensors] | Yes | Via `DcMotorEx.getCurrent` |
| Hub battery / bus current monitoring | REV lists battery current accessible; servo power bus current not accessible[^rev-sensors] | Partial | Use only documented accessible channels |
| XT30 connection integrity | REV troubleshooting for compressed XT30 pins[^rev-ch-trouble] | Yes (hardware) | Software cannot fix poor connectors |
| Internal Hub safety monitoring | REV notes some sensors used internally and not all user-accessible[^rev-sensors] | Yes | Opaque to team code |

### FRC (do not transplant blindly)

| Protection | Evidence | Direct FTC transfer? |
|------------|----------|----------------------|
| roboRIO staged brownout / rail disable | WPILib brownout doc[^wpilib-brownout] | No — different controller |
| PDP/PDH channel current + energy logging | WPILib Power Distribution Module[^wpilib-pd] | Conceptual only |
| TalonFX supply/stator current limits | CTRE current-limits guide[^ctre-current] | No — not REV Hub motor ports |
| Settable brownout voltage (roboRIO 2.0) | WPILib brownout doc[^wpilib-brownout] | No |

### SystemCore

| Claim | Label |
|-------|-------|
| SystemCore may improve electrical headroom or telemetry vs Duo Control | **FH / UH** until authoritative docs exist |
| AMPER implements SystemCore adapters | **False** — boundary only; see `SystemCoreAdapterBoundary` |

---

## 3. Measurements FTC code can obtain

### 3.1 `VoltageSensor`

- **VF:** RobotCore exposes `VoltageSensor#getVoltage()`.[^javadoc-voltage]
- **VF:** Hubs appear as voltage sensors in the hardware map; community and FIRST forum guidance shows reading Control Hub / Expansion Hub voltage in OpModes.[^ftc-forum-voltage][^javadoc-voltage]
- **EI:** Reported voltage is bus/battery terminal voltage as seen by the Hub, not a perfect open-circuit battery EMF.
- **Latency / bulk reads:** **UH/MR** — must be measured; treat values as loop-sampled with possible Hub communication delay.

### 3.2 `DcMotorEx.getCurrent`

- **VF:** `DcMotorEx#getCurrent(CurrentUnit)` returns motor current.[^javadoc-motor]
- **VF (community reference):** Game Manual 0 notes current readings are **not** bulk-read, while over-current alerts may be; this implies extra SDK/Lynx traffic when polled every loop.[^gm0-motors]
- **EI:** High-rate current polling of many motors can increase loop time; Phase 0 must measure overhead.

### 3.3 Hub voltage and current

- **VF:** REV documents accessible battery voltage and battery current, I2C/digital bus currents; servo power bus current not accessible.[^rev-sensors]
- **EI:** Battery current is valuable for load correlation but resolution/accuracy at low current is unverified here (**MR** required).

### 3.4 Commanded output, velocity, position

- **VF:** Motor power is a commanded fraction of input voltage implemented via PWM on REV hubs; power `1.0` tracks battery voltage, so effective torque/speed changes with sag.[^gm0-motors]
- **VF:** Encoder velocity/position available through SDK motor APIs (standard FTC).

---

## 4. Battery voltage sag and effective resistance

### Physics (engineering)

Under load, terminal voltage approximates:

\[
V_{terminal} \approx V_{oc} - I \cdot R_{effective}
\]

where \(R_{effective}\) includes battery internal resistance plus wiring/connector resistance.

| Statement | Label |
|-----------|-------|
| Sag increases with current and effective resistance | VF (circuit theory) |
| FTC code can directly read \(R_{internal}\) as a Hub register | Not supported |
| Estimating \(R_{effective}\) from \(\Delta V / \Delta I\) across load steps can work | EI / UH until validated |
| Weak batteries / high resistance connectors worsen sag | VF (REV battery care / troubleshooting context)[^rev-ch-trouble] |

FRC literature emphasizes proactive power budgeting and measuring battery health rather than relying only on reactive brownout.[^wpilib-brownout]

---

## 5. Motor startup, acceleration, stalls, regeneration

| Topic | Finding | Label |
|-------|---------|-------|
| Startup / acceleration | High current while accelerating inertia; simultaneous mechanisms compound sag | EI |
| Stalls / jams | Near-stall draws high current; detecting via current + lack of motion is common practice | EI |
| Regeneration | Brushed DC motors generate when back-driven; BRAKE shorts leads and resists motion[^gm0-motors] | VF (qualitative) |
| Software “pulsing” vs PWM | Hub already uses PWM for average voltage; team-level time slicing is a different, higher-level strategy | VF / EI |

---

## 6. Control techniques catalog

### Voltage compensation

- **EI:** Scaling commands by \(V_{nominal}/V_{measured}\) can stabilize autonomous feedforward when battery sags.
- **Caution:** Compensation **increases** command when voltage is low, which can worsen sag if used aggressively near brownout. **UH** that naive compensation is always safe.

### Current limiting

- **FRC VF:** TalonFX supports supply/stator current limits in firmware.[^ctre-current]
- **FTC:** Soft limiting is a **team-code** duty cycle / effort cap unless using a smart motor controller with native limits (not assumed for REV Hub ports).

### Slew-rate limiting

- **EI:** Limiting \(d(power)/dt\) reduces simultaneous inrush; common FRC/FTC teleop technique.
- **Transfer:** Yes, as software on FTC.

### Subsystem interlocks

- **VF (FRC guidance):** Power budgets may require mutually exclusive functions enforced in software.[^wpilib-brownout]
- **Transfer:** Yes, as policy in AMPER Phase 4.

---

## 7. What teams commonly implement

### FRC (common → advanced)

| Practice | Typical tier |
|----------|--------------|
| Watch bus voltage / DS brownout lights | Common |
| PDP/PDH logging | Common among competitive teams |
| Current limits on motor controllers | Common with CTRE/REV CAN devices |
| Dynamic mechanism disable when drivetrain current high | Advanced / situational |
| Online \(R_{eff}\) prediction with confidence gating | Rare / research-like |

### FTC (common → rare)

| Practice | Typical tier |
|----------|--------------|
| Telemetry of Hub voltage | Fairly common |
| Voltage-normalized autonomous drive | Moderately common |
| Per-motor current telemetry | Less common (overhead + awareness) |
| Robot-wide priority power coordinator | Rare / novel for most FTC teams |
| Predictive sag model with shadow mode | Rare / research |

---

## 8. What transfers to FTC vs what does not

| Technique | Transfers? | Why |
|-----------|------------|-----|
| Measure voltage + correlate with commands | Yes | SDK supports it |
| Slew limits / output caps / stall timeouts | Yes | Pure software |
| Priority allocation of effort | Yes | Pure software |
| Firmware current limits like TalonFX | No (on Hub ports) | Different hardware |
| roboRIO staged brownout rails | No | Different controller |
| PDP energy integration APIs | Conceptual only | No PDP in FTC Duo Control |
| SystemCore unknowns | Not yet | **FH** |

---

## 9. Limits of a software-only solution

Software cannot:

- repair high-resistance XT30 pins or damaged wiring;[^rev-ch-trouble]
- restore capacity to a failed pack;
- invent current data when sensors are missing;
- outrun physics when instantaneous demand exceeds battery delivery;
- replace mechanical holding devices on gravity loads (see elevator notes in [architecture](architecture.md)).

AMPER’s honest role is **awareness**, **avoidable demand shaping**, and **education**.

---

## 10. Capability matrix for AMPER design

| # | Category | Examples | AMPER implication |
|---|----------|----------|-------------------|
| 1 | Firmware in roboRIO/VEX/etc. | Staged brownout | Study only |
| 2 | Smart FRC motor controllers | TalonFX current limits | Study only |
| 3 | Current FTC SDK + REV Hub | `VoltageSensor`, `DcMotorEx.getCurrent` | Phase 0 adapters |
| 4 | Announced SystemCore features | TBD | Document when authoritative |
| 5 | Unverified SystemCore | Anything without primary docs | Do not claim |
| 6 | Must implement in team code | Filters, policy, coordination | AMPER core |
| 7 | Novel proposed AMPER behavior | Predictive load shaping | Experimental phases 5–6 |

---

## 11. Native WPILOG on the current Control Hub (decision)

**VF:** The WPILib Data Log format is specified independently of FRC hardware (`datalog.adoc` v1.0). AdvantageScope can open WPILOG and generic CSV.

**VF:** FTC SDK 11.2.0 / FtcRobotController v11.2 (accessed 2026-08-17) does not include WPILib `DataLog` or a documented JNI DataLog on the REV Control Hub.

**EI:** Shipping WPILib native libraries on the current Control Hub would add an uncharacterized Android NDK/JNI dependency (package size, CPU, flash wear). That experiment was **not** done for this release.

**Decision:** Robot-side format is AdvantageScope CSV. WPILOG is produced by `amper-tools` on a desktop. When SystemCore publishes an authoritative logging API, add a **separate adapter** rather than changing AMPER’s canonical `/AMPER` event model.

CTRE Hoot and REVLOG remain vendor formats. AMPER does not require them at runtime.

---

## Footnotes

[^rev-ch-trouble]: REV Robotics, “Control Hub Troubleshooting,” Duo Control docs. https://docs.revrobotics.com/duo-control/troubleshooting-the-control-system/control-hub-troubleshooting — brownout symptom list including ~9 V displayed and DS disconnects.

[^rev-sensors]: REV Robotics, “Integrated Sensors,” Duo Control docs. https://docs.revrobotics.com/duo-control/control-system-overview/integrated-sensors — accessible voltage/current vs not accessible servo-bus current.

[^wpilib-brownout]: WPILib, “roboRIO Brownout and Understanding Current Draw.” https://docs.wpilib.org/en/stable/docs/software/roborio-info/roborio-brownouts.html — staged triggers, power budgeting, PDP usage.

[^wpilib-pd]: WPILib, “Power Distribution Module.” https://docs.wpilib.org/en/stable/docs/software/can-devices/power-distribution-module.html — bus voltage/channel current APIs.

[^ctre-current]: CTRE, “Improving Performance with Current Limits” (Talon FX). https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html — firmware current limiting (FRC).

[^javadoc-voltage]: FIRST Tech Challenge RobotCore Javadoc, `VoltageSensor`. https://javadoc.io/doc/org.firstinspires.ftc/RobotCore — `getVoltage()`.

[^javadoc-motor]: FIRST Tech Challenge RobotCore Javadoc, `DcMotorEx`. https://javadoc.io/doc/org.firstinspires.ftc/RobotCore — `getCurrent(CurrentUnit)`.

[^ftc-forum-voltage]: FIRST Tech Challenge Community, “Using a Voltage Sensor.” https://ftc-community.firstinspires.org/t/using-a-voltage-sensor/464 — Control Hub voltage read example.

[^gm0-motors]: Game Manual 0, “SDK Motors.” https://gm0.org/en/latest/docs/software/adv-control-system/sdk-motors.html — PWM average voltage, BRAKE/FLOAT, `DcMotorEx`, current not bulk-read.

See also the reference table in [references.md](references.md).
