# Priority ledger

Updated: 2026-08-18. Synced to `main` `98003745dfa3a86525b036f0d9568772dc0f3568`. Orchestrator identity `TA-C-GHill`. `AUTOMATIC_MERGE=false`.

Priority model: safety blockers → correctness blockers → CI/build → unblocking issues → architectural seams → tests for upcoming work → small user-facing slices → measured performance → docs → optional advanced → cosmetic.

An issue is **ready** only when requirements are clear, dependencies are resolved, hardware is available or unnecessary, and no overlapping implementation PR is open.

**First implementation/readiness priority:** [#41](https://github.com/The-Allsparks/AMPER/issues/41) (FTC integration reference). Combined-stack acceptance is [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4). Tracking epic for 0.1.x phases remains [#24](https://github.com/The-Allsparks/AMPER/issues/24).

Desktop `./gradlew check` and `sdk-compile` are **not** Control Hub validation. Do not claim FTC-ready from desktop tests, architecture docs, or TeamCode sketches.

## In-flight

| Issue | Priority | Readiness | Dependencies | Status | Subagent | Branch | Pull request | CI | Merge | Blocker | Next action |
|-------|----------|-----------|--------------|--------|----------|--------|--------------|----|-------|---------|-------------|
| [#42](https://github.com/The-Allsparks/AMPER/issues/42) Record #41 as first priority | HIGH | Ready | none | This docs PR | orchestrator | `docs/42-ftc-integration-priority` | this PR | pending | not authorized | Human merge after required checks |

No other implementation PR is open. Dependabot [#21](https://github.com/The-Allsparks/AMPER/pull/21) (Gradle 9.7) failed — do not merge. [#23](https://github.com/The-Allsparks/AMPER/pull/23) (checkout 4→7) is green and unreviewed; SHA pinning remains [#30](https://github.com/The-Allsparks/AMPER/issues/30).

## First readiness epic

| Issue | Priority | Readiness | Dependencies | Status | Blocker | Next action |
|-------|----------|-----------|--------------|--------|---------|-------------|
| [#41](https://github.com/The-Allsparks/AMPER/issues/41) FTC integration reference | HIGH | Epic; first child is #42 | Hub evidence for cost items; FORGE#4 for combined stack | Open | Hardware for disabled/passive cost; combined stack is external | After #42: [#43](https://github.com/The-Allsparks/AMPER/issues/43) packaging guard |
| [#42](https://github.com/The-Allsparks/AMPER/issues/42) Ledger/roadmap list #41 first | HIGH | This PR | none | In flight | none | Merge this docs PR |
| [#43](https://github.com/The-Allsparks/AMPER/issues/43) Stubs/tools off robot artifacts | HIGH | Ready after #42 | none | Open | none | Next #41 software slice |
| [#28](https://github.com/The-Allsparks/AMPER/issues/28) Init vs match lifecycle | MEDIUM | Ready | none | Open | none | After #43 |
| [#25](https://github.com/The-Allsparks/AMPER/issues/25) Protect `main` | HIGH | Partial | Human review-count decision | Open | Policy | Protection **exists**; remaining: document policy, decide review count, require `sdk-compile` |
| [#44](https://github.com/The-Allsparks/AMPER/issues/44) Sibling electrical contracts | MEDIUM | Ready | #42 preferred | Open | none | Docs contract; no sibling JARs |
| [#6](https://github.com/The-Allsparks/AMPER/issues/6) Hub characterization | HIGH | **Blocked** | Control Hub | Open | Hardware unavailable | Human robot session; #41 consumes these numbers |

## Existing phase issues

| Issue | Priority | Readiness | Dependencies | Status | Blocker | Next action |
|-------|----------|-----------|--------------|--------|---------|-------------|
| [#1](https://github.com/The-Allsparks/AMPER/issues/1) Research | MEDIUM | Ready for maintainer close review | none | Software done | Citation review | Comment + optional close after review |
| [#2](https://github.com/The-Allsparks/AMPER/issues/2) Phase 0 abstraction | MEDIUM | Software done; hardware not | #6 for Hub evidence | Open | Hardware | Split software vs #6 |
| [#3](https://github.com/The-Allsparks/AMPER/issues/3) REV adapter | MEDIUM | Software done; hardware not | #6 | Open | Hardware | Keep until Hub data |
| [#4](https://github.com/The-Allsparks/AMPER/issues/4) Logging | MEDIUM | Software done; on-robot retrieve not | #6 | Open | Hardware retrieve | Keep or split |
| [#5](https://github.com/The-Allsparks/AMPER/issues/5) Phase 1 | MEDIUM | Software done; hardware not | #6 | Open | Hardware | Keep hardware on #6 |
| [#6](https://github.com/The-Allsparks/AMPER/issues/6) Characterization | HIGH | **Blocked** | Control Hub | Open | Hardware unavailable in this environment | Human robot session |
| [#7](https://github.com/The-Allsparks/AMPER/issues/7) Phase 2 slew | HIGH | **Blocked** | #6, flag gate, #41 cost evidence | Open | Readiness gate | Do not implement actuation |
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
| Roadmap | [#24](https://github.com/The-Allsparks/AMPER/issues/24) | HIGH | Tracking | none | Open epic | 0.1.x tracking; does **not** outrank #41 |
| FTC integration | [#41](https://github.com/The-Allsparks/AMPER/issues/41) | HIGH | First readiness priority | #6 for Hub cost; FORGE#4 combined | Open epic | After #42 |
| R2 | PR [#18](https://github.com/The-Allsparks/AMPER/pull/18) | HIGH | **Done** | none | Merged 2026-08-17 | `0.1.0-rc.1` software is on `main`; git tag still missing |
| U6 | [#34](https://github.com/The-Allsparks/AMPER/issues/34) | HIGH | **Done** | none | Closed via [#37](https://github.com/The-Allsparks/AMPER/pull/37) | — |
| C7 | [#33](https://github.com/The-Allsparks/AMPER/issues/33) | HIGH | **Done** | none | Closed via [#38](https://github.com/The-Allsparks/AMPER/pull/38) | — |
| C8 | [#35](https://github.com/The-Allsparks/AMPER/issues/35) | HIGH | **Done** | none | Closed via [#39](https://github.com/The-Allsparks/AMPER/pull/39) | — |
| Dep4 | [#36](https://github.com/The-Allsparks/AMPER/issues/36) | HIGH | **Done** (desktop SDK compile) | none | Closed via [#40](https://github.com/The-Allsparks/AMPER/pull/40) | `sdk-compile` exists; **not** a required check; not Hub validation |
| R1 | [#25](https://github.com/The-Allsparks/AMPER/issues/25) Protect `main` | HIGH | Partial | Human policy | Open | Protection exists; review count 0; `sdk-compile` not required; docs still missing |
| Dep1 | [#29](https://github.com/The-Allsparks/AMPER/issues/29) Gradle/JUnit majors | HIGH | Ready | none | Open; [#20](https://github.com/The-Allsparks/AMPER/pull/20) closed unmerged; [#21](https://github.com/The-Allsparks/AMPER/pull/21) still open **failed** | Close #21 with a pointer here; group Dependabot |
| A3 / S5 | [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection flag gate | HIGH | Ready as a seam; **after #41** | none for the seam itself | Open | Do not outrank FTC integration; do not enable Phase 2 |
| C6 / S6 | [#27](https://github.com/The-Allsparks/AMPER/issues/27) Gravity hold direction | HIGH | **Blocked** | #6, #26, #7 | Open | Do not implement actuation |
| C1 | [#28](https://github.com/The-Allsparks/AMPER/issues/28) `observe()` match reset | MEDIUM | Ready | none | Open | Child of #41 lifecycle |
| P1 | [#6](https://github.com/The-Allsparks/AMPER/issues/6) Hub overhead | HIGH | **Blocked** | Hardware | Open | Hardware |
| P2 / P3 | [#31](https://github.com/The-Allsparks/AMPER/issues/31) Logger/allocation baseline | MEDIUM | Ready | none | Open | Desktop bench, then decide |
| Dep2 | [#30](https://github.com/The-Allsparks/AMPER/issues/30) Pin Actions SHAs | MEDIUM | Ready | none | Open; [#22](https://github.com/The-Allsparks/AMPER/pull/22) merged (setup-java v5); [#23](https://github.com/The-Allsparks/AMPER/pull/23) unreviewed | Prefer SHA pins over floating majors |
| U2 / D1 / D3 | Doc contradictions | MEDIUM | **Done** in PR #18 | none | Merged | — |
| D5 | [#32](https://github.com/The-Allsparks/AMPER/issues/32) SECURITY contact | LOW | Ready | Maintainer email | Open | Human |

## Stop conditions currently true

- Hardware for #6 is unavailable in this environment.
- Automatic merge is **not** authorized.
- Phase 2+ actuation is behind a readiness gate. Do not start #7–#16 because #41 exists.
- Do not claim FTC-ready from desktop tests, `sdk-compile`, or this ledger.
- Do not merge Dependabot [#21](https://github.com/The-Allsparks/AMPER/pull/21) (Gradle 9.7; CI failed).
