# AMPER roadmap (0.1.x)

This roadmap adapts the Allsparks orchestrator phases to AMPER. AMPER’s readiness gate is: **observe and characterize before actively limiting power**.

Parent tracking issue: [#24](https://github.com/The-Allsparks/AMPER/issues/24). Existing issues **#1–#16** remain the phase backlog.

**First implementation/readiness priority:** [#41](https://github.com/The-Allsparks/AMPER/issues/41) — establish AMPER as the validated FTC integration reference. Combined-stack acceptance is [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4). Desktop tests are not Control Hub validation.

Audit-derived issues include **#25–#36** (several closed), **#41–#44**, and the 2026-08-20 quality/performance epic **[#50](https://github.com/The-Allsparks/AMPER/issues/50)** (children **#51–#59**).

## Foundation

- Identity, license, modules, CI, research (#1).
- Installable `amper-core` / `amper-ftc` **on `main`** (merged [#18](https://github.com/The-Allsparks/AMPER/pull/18)).
- Honest maturity labels.

## Safety and correctness

- Phase 0/1 never write motors (enforced in tests).
- Missing/stale sensing stays invalid.
- Phase 2+ stay disabled until #6 and explicit flags.
- Remaining seams: gravity hold direction (#27). LocalProtection session flag gate (#26) is implemented for `fromPolicy` / `AmperSession.localProtection`. These stay **behind** Hub evidence for competition enable.

## Architecture stabilization

- Hardware-independent core + FTC adapters.
- Canonical `/AMPER` log model.
- SystemCore remains a boundary (#16).

## Passive observability

- Phase 0 measurement (#2, #3).
- Phase 1 warnings and summaries (#5).
- AdvantageScope CSV + desktop WPILOG (#4).

## Testing and simulation

- Desktop unit tests and replay traces (implemented).
- Official RobotCore 11.2 compile in CI (#36, done). Default `check` still uses stubs.
- Control Hub characterization (#6) — **required before 0.1.0 final** and for #41 cost acceptance.
- Desktop allocation microbench at production logger capacity (4000 samples; `ObservePerformanceBudgetTest`).

## Basic integration

- `AmperFtc.builder(hardwareMap)` examples.
- Composite `includeBuild` install path.
- **#41** packaging, lifecycle, and install hardening (software checklist done: [#43](https://github.com/The-Allsparks/AMPER/issues/43), [#28](https://github.com/The-Allsparks/AMPER/issues/28), [#44](https://github.com/The-Allsparks/AMPER/issues/44); Hub cost still [#6](https://github.com/The-Allsparks/AMPER/issues/6)). See [ftc-integration-checklist.md](ftc-integration-checklist.md).

## Advanced integration

- Not started in code. No ViDAR/Pedro/MIMIC/BEACON/TRACE/HELM compile-time coupling.
- Written contracts: [docs/integration/sibling-contracts.md](../integration/sibling-contracts.md) ([#44](https://github.com/The-Allsparks/AMPER/issues/44)). Combined proof: FORGE#4.

## Active behavior

- Phase 2 local protection (#7, #8) — **blocked on #6** and #41 cost evidence.
- Phase 3–7 (#9–#15) — **blocked on Phase 2 evidence**.

## Performance optimization

- Desktop observe budget: `ObservePerformanceBudgetTest` at **4000** samples (generous ceilings; not Hub SLAs). Implementation children [#51](https://github.com/The-Allsparks/AMPER/issues/51)–[#55](https://github.com/The-Allsparks/AMPER/issues/55) **merged**. Hub remains [#6](https://github.com/The-Allsparks/AMPER/issues/6).

## Release readiness

- [x] Merge PR #18 to `main`.
- [x] Tag `v0.1.0-rc.1` as **software** rc (cut after Phase 2 dual-opt-in #26 / #48).
- [x] Finish [#25](https://github.com/The-Allsparks/AMPER/issues/25): protection documented; `sdk-compile` required; review count remains 0 by solo-maintainer policy (raise when a second reviewer exists).
- [ ] `0.1.0` final only when `docs/validation/STATUS.md` is no longer “not yet run”.

## Completion definition for 0.1.0-rc.1

- [x] Passive library builds and tests on desktop CI
- [x] Examples compile
- [x] PR #18 merged to `main`
- [x] Tag `v0.1.0-rc.1` (human)
- [ ] Hardware characterization (#6) — **not** required for rc.1 software tag; **required** for `0.1.0`

## Child issues (dependency order)

Closed software slices (do not re-open as next work): [#18](https://github.com/The-Allsparks/AMPER/pull/18), [#34](https://github.com/The-Allsparks/AMPER/issues/34), [#33](https://github.com/The-Allsparks/AMPER/issues/33), [#35](https://github.com/The-Allsparks/AMPER/issues/35), [#36](https://github.com/The-Allsparks/AMPER/issues/36), [#42](https://github.com/The-Allsparks/AMPER/issues/42), [#43](https://github.com/The-Allsparks/AMPER/issues/43), [#28](https://github.com/The-Allsparks/AMPER/issues/28), [#44](https://github.com/The-Allsparks/AMPER/issues/44), [#1](https://github.com/The-Allsparks/AMPER/issues/1)–[#5](https://github.com/The-Allsparks/AMPER/issues/5) software, [#30](https://github.com/The-Allsparks/AMPER/issues/30)–[#32](https://github.com/The-Allsparks/AMPER/issues/32), [#50](https://github.com/The-Allsparks/AMPER/issues/50)–[#59](https://github.com/The-Allsparks/AMPER/issues/59).

1. [#41](https://github.com/The-Allsparks/AMPER/issues/41) FTC integration epic (**first readiness priority**; software done — Hub + FORGE remain)
2. [#6](https://github.com/The-Allsparks/AMPER/issues/6) Control Hub characterization (**hardware**; feeds #41 cost acceptance)
3. [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4) combined-stack acceptance (external)
4. [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection feature-flag gate (**software done**; competition enable still needs #6)
5. [#27](https://github.com/The-Allsparks/AMPER/issues/27) gravity hold direction (**blocked** on #6; avoid lifts/arms Phase 2 until fixed)
6. [#7](https://github.com/The-Allsparks/AMPER/issues/7)–[#16](https://github.com/The-Allsparks/AMPER/issues/16) remain behind readiness gates
7. [#24](https://github.com/The-Allsparks/AMPER/issues/24) roadmap epic (tracking only)
