# FTC integration checklist (#41)

Parent: [#41](https://github.com/The-Allsparks/AMPER/issues/41). Combined-stack acceptance: [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4).

Desktop `./gradlew check` and `sdk-compile` are **not** Control Hub validation.

## Software (done on `main`)

- [x] Module boundaries: `amper-core` / `amper-ftc` / stubs / examples / tools ([#43](https://github.com/The-Allsparks/AMPER/issues/43))
- [x] Stubs and desktop tools cannot ship on the RC ([#43](https://github.com/The-Allsparks/AMPER/issues/43), [install.md](../install.md))
- [x] Pinned FTC SDK compile in CI for adapters and examples ([#36](https://github.com/The-Allsparks/AMPER/issues/36))
- [x] Lifecycle: init / start / observe / stop; init samples out of match summaries ([#28](https://github.com/The-Allsparks/AMPER/issues/28))
- [x] AMPER owns no actuators on Phase 0/1 paths (architecture tests)
- [x] Sibling contracts for MIMIC, BEACON, HELM, TRACE ([#44](https://github.com/The-Allsparks/AMPER/issues/44), [sibling-contracts.md](../integration/sibling-contracts.md))
- [x] Student install + troubleshooting reference ([install.md](../install.md))
- [x] Roadmap/ledger list #41 first ([#42](https://github.com/The-Allsparks/AMPER/issues/42))
- [x] Phase 0/1 software backlog closed onto Hub evidence (#1–#5 → #6)

## Hardware / external (still open)

- [ ] Disabled-mode and passive-mode loop cost measured on a Control Hub ([#6](https://github.com/The-Allsparks/AMPER/issues/6))
- [ ] Record results in [validation/STATUS.md](../validation/STATUS.md) and [results-template.md](../validation/results-template.md)
- [ ] Combined-stack acceptance through [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4)

Do not close #41 until the hardware and FORGE rows are done. Do not enable Phase 2+ from this checklist alone.
