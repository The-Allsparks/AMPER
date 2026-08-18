# Priority ledger

Updated: 2026-08-17. Audited commit `794a8e3`. Orchestrator identity `TA-C-GHill`. `AUTOMATIC_MERGE=false`.

Priority model: safety blockers → correctness blockers → CI/build → unblocking issues → architectural seams → tests for upcoming work → small user-facing slices → measured performance → docs → optional advanced → cosmetic.

An issue is **ready** only when requirements are clear, dependencies are resolved, hardware is available or unnecessary, and no overlapping implementation PR is open.

## In-flight

| Issue | Priority | Readiness | Dependencies | Status | Subagent | Branch | Pull request | CI | Merge | Blocker | Next action |
|-------|----------|-----------|--------------|--------|----------|--------|--------------|----|-------|---------|-------------|
| 0.1.0-rc.1 software delivery | HIGH | Ready for human merge | none | Ready for review; audit follow-up pending this commit | orchestrator | `release/passive-0.1.0-rc.1` | [#18](https://github.com/The-Allsparks/AMPER/pull/18) | green on `3231ce5` | not authorized | Human merge approval | Do not merge automatically |

Do **not** start another implementation PR until #18 is merged, closed, or explicitly split.

## Existing phase issues

| Issue | Priority | Readiness | Dependencies | Status | Blocker | Next action |
|-------|----------|-----------|--------------|--------|---------|-------------|
| [#1](https://github.com/The-Allsparks/AMPER/issues/1) Research | MEDIUM | Ready for maintainer close review | none | Software done | Citation review | Comment + optional close after review |
| [#2](https://github.com/The-Allsparks/AMPER/issues/2) Phase 0 abstraction | MEDIUM | Software done; hardware not | #6 for Hub evidence | Open | Hardware | Split software vs #6 |
| [#3](https://github.com/The-Allsparks/AMPER/issues/3) REV adapter | MEDIUM | Software done; hardware not | #6 | Open | Hardware | Keep until Hub data |
| [#4](https://github.com/The-Allsparks/AMPER/issues/4) Logging | MEDIUM | Software done; on-robot retrieve not | #6 | Open | Hardware retrieve | Keep or split |
| [#5](https://github.com/The-Allsparks/AMPER/issues/5) Phase 1 | MEDIUM | Software done; hardware not | #6 | Open | Hardware | Keep hardware on #6 |
| [#6](https://github.com/The-Allsparks/AMPER/issues/6) Characterization | HIGH | **Blocked** | Control Hub | Open | Hardware unavailable in this environment | Human robot session |
| [#7](https://github.com/The-Allsparks/AMPER/issues/7) Phase 2 slew | HIGH | **Blocked** | #6, flag gate | Open | Readiness gate | Do not implement actuation |
| [#8](https://github.com/The-Allsparks/AMPER/issues/8) Phase 2 stall | HIGH | **Blocked** | #6, #7 | Open | Readiness gate | Warning-first remains |
| [#9](https://github.com/The-Allsparks/AMPER/issues/9) Phase 3 FSM | LOW | **Blocked** | Phase 2 evidence | Open | Readiness gate | Foundation only |
| [#10](https://github.com/The-Allsparks/AMPER/issues/10) PID/FF constraints | LOW | **Blocked** | Phase 3 | Open | Readiness gate | Type exists |
| [#11](https://github.com/The-Allsparks/AMPER/issues/11) Request/grant | LOW | **Blocked** | Phase 3 | Open | Readiness gate | Stub |
| [#12](https://github.com/The-Allsparks/AMPER/issues/12) Coordinator | LOW | **Blocked** | #11 | Open | Readiness gate | Pass-through |
| [#13](https://github.com/The-Allsparks/AMPER/issues/13) Shadow prediction | LOW | **Blocked** | #6 datasets | Open | Data + gate | Shadow only |
| [#14](https://github.com/The-Allsparks/AMPER/issues/14) Load shaping | LOW | **Blocked** | #13 | Open | Gate | Not implemented |
| [#15](https://github.com/The-Allsparks/AMPER/issues/15) Adaptive profiles | LOW | **Blocked** | #13 | Open | Gate | Not implemented |
| [#16](https://github.com/The-Allsparks/AMPER/issues/16) SystemCore | LOW | **Blocked** | Authoritative docs | Open | External docs | Keep boundary |

## Audit-derived work

| Audit ID | Issue | Priority | Readiness | Dependencies | Status | Next action |
|----------|-------|----------|-----------|--------------|--------|-------------|
| Roadmap | [#24](https://github.com/The-Allsparks/AMPER/issues/24) | HIGH | Tracking | none | Open epic | Update as PRs merge |
| R2 / in-flight | PR #18 | HIGH | Ready for human merge after this docs pass | none | Open draft | Human approval |
| R1 | [#25](https://github.com/The-Allsparks/AMPER/issues/25) Protect `main` | HIGH | Ready (settings) | Human GitHub admin | Open | Human decision |
| Dep1 | [#29](https://github.com/The-Allsparks/AMPER/issues/29) Gradle/JUnit majors | HIGH | Ready | none | Open; PRs #20 #21 failed | Close those PRs with a pointer here |
| A3 / S5 | [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection flag gate | HIGH | Ready after #18 | none for the seam itself | Open | After #34–#36 |
| C7 | [#33](https://github.com/The-Allsparks/AMPER/issues/33) Stall dwell vs SKIPPED current | HIGH | Ready after #18 | none | Open | After #34 |
| U6 | [#34](https://github.com/The-Allsparks/AMPER/issues/34) `publishTelemetry` when disabled | HIGH | Ready after #18 | none | Open | **First software issue after #18** |
| C8 | [#35](https://github.com/The-Allsparks/AMPER/issues/35) Weak-battery latch | HIGH | Ready after #18 | none | Open | After #33 |
| Dep4 | [#36](https://github.com/The-Allsparks/AMPER/issues/36) Compile against FTC SDK 11.2 in CI | HIGH | Ready after #18 | Maven resolution | Open | After #35; stop if secrets required |
| C6 / S6 | [#27](https://github.com/The-Allsparks/AMPER/issues/27) Gravity hold direction | HIGH | **Blocked** | #6, #26, #7 | Open | Do not implement actuation |
| C1 | [#28](https://github.com/The-Allsparks/AMPER/issues/28) `observe()` match reset | MEDIUM | Ready after #18 | none | Open | Small lifecycle test |
| P1 | [#6](https://github.com/The-Allsparks/AMPER/issues/6) Hub overhead | HIGH | **Blocked** | Hardware | Open | Hardware |
| P2 / P3 | [#31](https://github.com/The-Allsparks/AMPER/issues/31) Logger/allocation baseline | MEDIUM | Ready after #18 | none | Open | Desktop bench, then decide |
| Dep2 | [#30](https://github.com/The-Allsparks/AMPER/issues/30) Pin Actions SHAs | MEDIUM | Ready after #18 | none | Open; PRs #22 #23 unreviewed | Review separately |
| U2 / D1 / D3 | Doc contradictions | MEDIUM | Done in PR #18 docs pass | none | In this branch | — |
| D5 | [#32](https://github.com/The-Allsparks/AMPER/issues/32) SECURITY contact | LOW | Ready | Maintainer email | Open | Human |

## Stop conditions currently true

- Hardware for #6 is unavailable in this environment.
- Automatic merge is **not** authorized.
- Phase 2+ actuation is behind a readiness gate.
- A second implementation PR must not open while #18 is unresolved.
