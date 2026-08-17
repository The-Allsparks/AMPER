# AMPER

**Adaptive Motor Power and Energy Regulation for FTC**

AMPER is a phased, robot-wide electrical-awareness and power-coordination
system for FIRST Tech Challenge robots.

It begins as passive instrumentation: measuring voltage, motor current,
commands, and voltage sag without changing robot behavior. Teams can then
progressively enable local mechanism protection, reactive voltage limiting,
priority-based load coordination, and—after collecting sufficient real-world
data—predictive voltage-sag management.

AMPER is designed to help students understand not only what the robot is
doing, but why its electrical system behaves that way.

---

## Built by The Allsparks

AMPER is created and maintained by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **#36117**).

It complements the team’s [ViDAR](https://github.com/The-Allsparks/ViDAR) project:

* **ViDAR** gives the robot situational awareness of the field.
* **AMPER** gives the robot situational awareness of its electrical system.

Repository: **[The-Allsparks/AMPER](https://github.com/The-Allsparks/AMPER)**

> **Disclaimer:** AMPER is community-developed and unofficial. It is **not** affiliated with or endorsed by FIRST, REV Robotics, CTRE, NI, or other referenced vendors. Teams must verify legality and performance against the current-season FTC Game Manual.

---

## Current status

| Item | Status |
|------|--------|
| **Version** | `0.1.0-SNAPSHOT` |
| **Implemented phase** | **Phase 0 — Measurement validation** (passive only) |
| **Phase 1** | Documented; not feature-complete |
| **Phases 2–7** | Designed / experimental / disabled by default |
| **Active motor intervention** | **Disabled.** Do not enable without review and acceptance tests. |
| **Predictive power management** | **Not production-ready.** Shadow/research only after real-robot data exists. |

**No phase should be enabled in competition without testing on your robot.**

Supported targets for this scaffold:

* **FTC SDK:** current public [FtcRobotController](https://github.com/FIRST-Tech-Challenge/FtcRobotController) season releases (Java TeamCode integration).
* **Hardware:** REV Control Hub and Expansion Hub electrical telemetry exposed through the FTC SDK (`VoltageSensor`, `DcMotorEx` current where available).
* **Library build:** Java 11 source/target; CI uses Temurin 17 to compile and test.

### Current limitations

* Phase 0 provides interfaces, REV adapters (supplier-wired), filtering, logging foundations, and unit tests. It does **not** change motor output.
* Hub/motor current sampling cost, latency, and reliability must be measured on your robot before trusting warnings.
* AMPER does **not** implement FRC-style roboRIO staged brownout firmware, TalonFX supply current limits, or unverified SystemCore features.
* Multi-hub timing, servo-rail current, and regenerative edge cases are only partially characterized in documentation.

### Software vs hardware brownout protection

Hardware and firmware protections (Hub resets, Driver Station disconnect symptoms, motor-controller behavior) react when voltage is already unsafe. AMPER’s later phases aim to **reduce avoidable demand** before that point. Software cannot replace:

* healthy batteries;
* tight XT30 / power connectors;
* correct wire gauge and short high-current paths;
* mechanical freedom (no stalls from binding).

**AMPER cannot repair bad batteries, loose connectors, damaged wiring, or undersized electrical paths.** Software protection **complements** good electrical construction; it does not replace it.

---

## Documentation

| Doc | Purpose |
|-----|---------|
| [Power management overview](docs/power-management/README.md) | Student entry point |
| [Research](docs/power-management/research.md) | Source-backed findings |
| [Architecture](docs/power-management/architecture.md) | Module boundaries |
| [Phases](docs/power-management/phases.md) | Phase goals and acceptance |
| [Integration](docs/power-management/integration.md) | OpMode loop contract |
| [Testing](docs/power-management/testing.md) | Unit / sim / robot procedures |
| [Tuning](docs/power-management/tuning.md) | Thresholds and flags |
| [Troubleshooting](docs/power-management/troubleshooting.md) | Failure modes |
| [Glossary](docs/power-management/glossary.md) | Vocabulary |
| [References](docs/power-management/references.md) | Citation table |
| [Examples](examples/README.md) | Integration sketches |
| [Phase 0 file plan](docs/power-management/phase-0-plan.md) | Exact implementation plan |
| [Assessment](docs/power-management/assessment.md) | Benefit vs complexity judgment |
| [Risks](docs/power-management/risks.md) | Open questions |

---

## Quick start (desktop)

```powershell
git clone https://github.com/The-Allsparks/AMPER.git
cd AMPER
.\gradlew.bat test
```

On Linux/macOS:

```bash
./gradlew test
```

---

## Design principles

1. **Passive first.** Measure and teach before intervening.
2. **Feature-flagged phases.** Each phase is independently testable and reversible.
3. **Fail safe.** Missing sensors disable intervention; they do not invent trust.
4. **Subsystem ownership.** AMPER constrains allowable effort; it does not replace PID, feedforward, or mechanism safety.
5. **Honest maturity.** Predictive features stay experimental until quantified on hardware.

---

## License

MIT — same open-source license family as [ViDAR](https://github.com/The-Allsparks/ViDAR). See [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md).
