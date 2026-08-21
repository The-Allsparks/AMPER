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
- Desktop allocation microbench (predicted performance only).

## Basic integration

- `AmperFtc.builder(hardwareMap)` examples.
- Composite `includeBuild` install path.
- **#41** packaging, lifecycle, and install hardening ([#43](https://github.com/The-Allsparks/AMPER/issues/43) merged; [#28](https://github.com/The-Allsparks/AMPER/issues/28) lifecycle slice in progress).

## Advanced integration

- Not started in code. No ViDAR/Pedro/MIMIC/BEACON/TRACE/HELM compile-time coupling.
- Written contracts: [docs/integration/sibling-contracts.md](../integration/sibling-contracts.md) ([#44](https://github.com/The-Allsparks/AMPER/issues/44)). Combined proof: FORGE#4.

## Active behavior

- Phase 2 local protection (#7, #8) — **blocked on #6** and #41 cost evidence.
- Phase 3–7 (#9–#15) — **blocked on Phase 2 evidence**.

## Performance optimization

- Only after measured Hub data or a desktop microbench. Do not tune from folklore.
- Desktop observe budget: `ObservePerformanceBudgetTest` (generous ceilings; not Hub SLAs). Implementation children: [#51](https://github.com/The-Allsparks/AMPER/issues/51)–[#55](https://github.com/The-Allsparks/AMPER/issues/55). Hub remains [#6](https://github.com/The-Allsparks/AMPER/issues/6).

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

Closed software slices (do not re-open as next work): [#18](https://github.com/The-Allsparks/AMPER/pull/18), [#34](https://github.com/The-Allsparks/AMPER/issues/34), [#33](https://github.com/The-Allsparks/AMPER/issues/33), [#35](https://github.com/The-Allsparks/AMPER/issues/35), [#36](https://github.com/The-Allsparks/AMPER/issues/36), [#42](https://github.com/The-Allsparks/AMPER/issues/42).

1. [#41](https://github.com/The-Allsparks/AMPER/issues/41) FTC integration epic (**first readiness priority**)
2. [#43](https://github.com/The-Allsparks/AMPER/issues/43) Keep stubs and desktop tools off robot-facing artifacts (this slice)
3. [#28](https://github.com/The-Allsparks/AMPER/issues/28) init vs match lifecycle
4. [#25](https://github.com/The-Allsparks/AMPER/issues/25) remaining branch-protection policy (`sdk-compile` required check; review count; docs)
5. [#44](https://github.com/The-Allsparks/AMPER/issues/44) sibling electrical contracts (docs; no sibling JARs)
6. [#29](https://github.com/The-Allsparks/AMPER/issues/29) Dependabot major policy (do not merge [#21](https://github.com/The-Allsparks/AMPER/pull/21))
7. [#1](https://github.com/The-Allsparks/AMPER/issues/1)–[#5](https://github.com/The-Allsparks/AMPER/issues/5) software vs hardware split onto #6
8. [#6](https://github.com/The-Allsparks/AMPER/issues/6) Control Hub characterization (**hardware**; also feeds #41 cost acceptance)
9. [#30](https://github.com/The-Allsparks/AMPER/issues/30) pin Actions SHAs
10. [#31](https://github.com/The-Allsparks/AMPER/issues/31) desktop logger baseline (budget test added; follow-ups [#51](https://github.com/The-Allsparks/AMPER/issues/51)–[#53](https://github.com/The-Allsparks/AMPER/issues/53))
11. [#50](https://github.com/The-Allsparks/AMPER/issues/50) quality/CI epic (does not outrank #41/#6)
12. [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection feature-flag gate (**software done**; competition enable still needs #6)
13. [#27](https://github.com/The-Allsparks/AMPER/issues/27) gravity hold direction (**blocked** on #6; avoid lifts/arms Phase 2 until fixed)
14. [#32](https://github.com/The-Allsparks/AMPER/issues/32) security contact (human)
15. [#7](https://github.com/The-Allsparks/AMPER/issues/7)–[#16](https://github.com/The-Allsparks/AMPER/issues/16) remain behind readiness gates
