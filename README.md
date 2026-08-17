# AMPER

**Adaptive Motor Power and Energy Regulation for FTC**

AMPER is the electrical-awareness companion to [ViDAR](https://github.com/The-Allsparks/ViDAR):

* **ViDAR** gives the robot situational awareness of the field.
* **AMPER** gives the robot situational awareness of its electrical system.

It is a phased system for FIRST Tech Challenge robots. The first usable release is **passive**: measure voltage (and optional motor current), warn, log, and export. It does **not** change motor output.

---

## Built by The Allsparks

AMPER is created and maintained by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **#36117**).

Repository: **[The-Allsparks/AMPER](https://github.com/The-Allsparks/AMPER)**

> **Disclaimer:** AMPER is community-developed and unofficial. It is **not** affiliated with or endorsed by FIRST, REV Robotics, CTRE, NI, or other referenced vendors. Teams must verify legality and performance against the current-season FTC Game Manual.

---

## Current status

| Item | Status |
|------|--------|
| **Version** | `0.1.0-rc.1` (prerelease) |
| **Implemented phase** | **Phase 0** (measurement) and **Phase 1** (passive telemetry, flag default off; enable with `AmperPolicies.passiveDefaults()`) |
| **Phase 2** | Experimental foundations, **disabled by default**, not robot-characterized |
| **Phases 3–7** | Designed / experimental / **disabled**. Not production-ready |
| **Active motor intervention** | **Disabled.** Phase 0/1 never call `setPower` or `setVelocity` |
| **Hardware validation** | **Not yet run.** See [docs/validation/STATUS.md](docs/validation/STATUS.md) |
| **SystemCore** | Adapter boundary only. Blocked on authoritative docs (issue #16) |

**No phase should be enabled in competition without testing on your robot.** Desktop tests are not Control Hub validation.

Roadmap vs code: [docs/status/issue-matrix.md](docs/status/issue-matrix.md).

### Current limitations

* Phase 0–1 provide measurement, logging, driver warnings, and match summaries. They do **not** change motor output.
* Hub/motor current sampling cost must be measured on **your** robot ([hardware test card](docs/validation/hardware-test-card.md)).
* AMPER does **not** implement FRC-style roboRIO staged brownout firmware, TalonFX supply current limits, or unverified SystemCore features.
* Total battery current is **not** claimed. Per-motor current, hub voltage, and inferred demand are distinct.
* Placeholder voltage thresholds are **conservative placeholders**, not universal FTC truth.

### Software vs hardware brownout protection

Hardware and firmware protections (Hub resets, Driver Station disconnect symptoms, motor-controller behavior) react when voltage is already unsafe. AMPER’s later phases aim to **reduce avoidable demand** before that point. Software cannot replace healthy batteries, tight XT30 / power connectors, correct wire gauge, or mechanical freedom.

**AMPER cannot repair bad batteries, loose connectors, damaged wiring, or undersized electrical paths.**

---

## Install and five-minute setup

1. [Install into an FTC SDK project](docs/install.md) (Gradle composite build preferred).
2. [Five-minute passive setup](docs/quickstart.md).
3. Copy an example from [`amper-examples`](amper-examples) (remove `@Disabled`).

```java
AmperSession amper = AmperFtc.builder(hardwareMap)
    .controlHubVoltage()
    .expansionHubVoltage("Expansion Hub 1") // optional, explicit name
    .observeMotor("frontLeft", frontLeft)
    .observeMotor("frontRight", frontRight)
    .observeMotor("lift", lift)
    .policy(AmperPolicies.passiveDefaults())
    .build();
```

Call `amper.initialize()` from `init`, `amper.start()` when the match starts, `amper.observe()` **once** per loop, `amper.publishTelemetry(...)` for rate-limited DS lines, and `amper.stop()` from `stop` so the AdvantageScope CSV is written.

Disable without changing motors: `AmperPolicies.disabled()` or `AmperPolicies.measurementOnly()`.

---

## Documentation

| Doc | Purpose |
|-----|---------|
| [Install](docs/install.md) | FTC SDK dependency / includeBuild |
| [Five-minute setup](docs/quickstart.md) | First telemetry |
| [Issue matrix](docs/status/issue-matrix.md) | Issues #1–#16 vs code |
| [Logging and export](docs/logging.md) | AdvantageScope CSV, `/AMPER` keys, WPILOG converter |
| [Compatibility](docs/compatibility.md) | FTC SDK / Java matrix |
| [Release](docs/release.md) | SemVer, checklist, artifacts |
| [Hardware validation](docs/validation/STATUS.md) | Not yet run |
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
| [Examples](examples/README.md) | Compile-checked OpModes |
| [Phase 0 file plan](docs/power-management/phase-0-plan.md) | Historical scaffold plan |
| [Assessment](docs/power-management/assessment.md) | Benefit vs complexity |
| [Risks](docs/power-management/risks.md) | Open questions |

---

## Desktop build

```powershell
git clone https://github.com/The-Allsparks/AMPER.git
cd AMPER
.\gradlew.bat check
```

On Linux/macOS:

```bash
./gradlew check
```

Modules: `amper-core` (pure Java), `amper-ftc` (FTC SDK types), `amper-examples` (compile-checked OpModes), `amper-tools` (CSV analysis). `amper-ftc-stubs` is CI-only and is **not** a robot dependency.

---

## Design principles

1. **Passive first.** Measure and teach before intervening.
2. **Feature-flagged phases.** Each phase is independently testable and reversible.
3. **Fail safe.** Missing sensors disable intervention; they do not invent trust.
4. **Subsystem ownership.** AMPER constrains allowable effort only when a later phase is explicitly enabled; it does not replace PID, feedforward, or mechanism safety.
5. **Honest maturity.** Predictive features stay experimental until quantified on hardware.

---

## License

MIT — same open-source license family as [ViDAR](https://github.com/The-Allsparks/ViDAR). See [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md).
