# AMPER roadmap (0.1.x)

This roadmap adapts the Allsparks orchestrator phases to AMPER. AMPER’s readiness gate is: **observe and characterize before actively limiting power**.

Parent tracking issue: [#24](https://github.com/The-Allsparks/AMPER/issues/24). Existing issues **#1–#16** remain the phase backlog. Audit-derived issues: **#25–#36**.

## Foundation

- Identity, license, modules, CI, research (#1).
- Installable `amper-core` / `amper-ftc` (draft PR #18).
- Honest maturity labels.

## Safety and correctness

- Phase 0/1 never write motors (enforced in tests).
- Missing/stale sensing stays invalid.
- Phase 2+ stay disabled until #6 and explicit flags.
- Remaining seams: LocalProtection flag gate; gravity hold direction.

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
- Control Hub characterization (#6) — **required before 0.1.0 final**.
- Desktop allocation microbench (predicted performance only).

## Basic integration

- `AmperFtc.builder(hardwareMap)` examples.
- Composite `includeBuild` install path.

## Advanced integration

- Not started. No ViDAR/Pedro/TRACE compile-time coupling.

## Active behavior

- Phase 2 local protection (#7, #8) — **blocked on #6**.
- Phase 3–7 (#9–#15) — **blocked on Phase 2 evidence**.

## Performance optimization

- Only after measured Hub data or a desktop microbench. Do not tune from folklore.

## Release readiness

- Merge PR #18 to `main` (human approval).
- Tag `v0.1.0-rc.1` as **software** rc.
- Branch protection on `main`.
- `0.1.0` final only when `docs/validation/STATUS.md` is no longer “not yet run”.

## Completion definition for 0.1.0-rc.1

- [x] Passive library builds and tests on desktop CI
- [x] Examples compile
- [ ] PR #18 merged to `main` (human)
- [ ] Tag `v0.1.0-rc.1` (human)
- [ ] Hardware characterization (#6) — **not** required for rc.1 software tag; **required** for `0.1.0`

## Child issues (dependency order)

1. [#18](https://github.com/The-Allsparks/AMPER/pull/18) land 0.1.0-rc.1 software (in flight)
2. [#34](https://github.com/The-Allsparks/AMPER/issues/34) `publishTelemetry` when Phase 1 is off
3. [#33](https://github.com/The-Allsparks/AMPER/issues/33) stall dwell vs SKIPPED current
4. [#35](https://github.com/The-Allsparks/AMPER/issues/35) weak-battery latch
5. [#36](https://github.com/The-Allsparks/AMPER/issues/36) SDK 11.2 CI compile
6. [#25](https://github.com/The-Allsparks/AMPER/issues/25) branch protection
7. [#29](https://github.com/The-Allsparks/AMPER/issues/29) Dependabot major policy
8. [#1](https://github.com/The-Allsparks/AMPER/issues/1)–[#5](https://github.com/The-Allsparks/AMPER/issues/5) software vs hardware split onto #6
9. [#6](https://github.com/The-Allsparks/AMPER/issues/6) Control Hub characterization (**hardware**)
10. [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection feature-flag gate
11. [#28](https://github.com/The-Allsparks/AMPER/issues/28) init vs match lifecycle
12. [#30](https://github.com/The-Allsparks/AMPER/issues/30) pin Actions
13. [#31](https://github.com/The-Allsparks/AMPER/issues/31) desktop logger baseline
14. [#27](https://github.com/The-Allsparks/AMPER/issues/27) gravity hold direction (**blocked** on #6/#26)
15. [#32](https://github.com/The-Allsparks/AMPER/issues/32) security contact (human)
16. [#7](https://github.com/The-Allsparks/AMPER/issues/7)–[#16](https://github.com/The-Allsparks/AMPER/issues/16) remain behind readiness gates
