# Power management documentation

AMPER teaches and eventually coordinates robot electrical demand on FIRST Tech Challenge robots.

## Start here

1. [Install AMPER](../install.md) and complete the [five-minute setup](../quickstart.md).
2. Read the [glossary](glossary.md).
3. Skim [research](research.md) for what FTC can actually measure.
4. Read [architecture](architecture.md) before changing control behavior.
5. Follow [phases](phases.md); enable only one phase at a time after acceptance tests.
6. Use [integration](integration.md) for the OpMode loop contract.

## Companion project

- [ViDAR](https://github.com/The-Allsparks/ViDAR) — field situational awareness
- AMPER — electrical situational awareness

## Hard rules

- Phase 0 and Phase 1 must not change motor outputs.
- Predictive features are not production-ready until quantified on real hardware.
- Software complements, and never replaces, sound electrical construction.

## Index

| Doc | Purpose |
|-----|---------|
| [research.md](research.md) | Source-backed findings |
| [architecture.md](architecture.md) | Module boundaries |
| [phases.md](phases.md) | Phase goals and acceptance |
| [integration.md](integration.md) | OpMode loop contract |
| [testing.md](testing.md) | Unit / sim / robot procedures |
| [tuning.md](tuning.md) | Thresholds and flags |
| [troubleshooting.md](troubleshooting.md) | Failure modes |
| [glossary.md](glossary.md) | Vocabulary |
| [references.md](references.md) | Citation table |
| [phase-0-plan.md](phase-0-plan.md) | Exact implementation plan |
| [assessment.md](assessment.md) | Benefit vs complexity judgment |
| [risks.md](risks.md) | Open questions |
| [conventions.md](conventions.md) | Org convention assessment |
| [Examples](../../examples/README.md) | Compile-checked OpModes |
| [../install.md](../install.md) | FTC SDK install |
| [../logging.md](../logging.md) | CSV export |
| [../status/issue-matrix.md](../status/issue-matrix.md) | Issues vs implementation |
| [../validation/STATUS.md](../validation/STATUS.md) | Hardware status (not yet run) |
