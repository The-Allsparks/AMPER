# Priority ledger

Updated: 2026-08-22. Synced to `main` after #72–#75 merges.

Priority model: safety blockers → correctness blockers → CI/build → unblocking issues → architectural seams → tests for upcoming work → small user-facing slices → measured performance → docs → optional advanced → cosmetic.

An issue is **ready** only when requirements are clear, dependencies are resolved, hardware is available or unnecessary, and no overlapping implementation PR is open.

## 2026-08-20 quality / performance audit

Scores and CI notes: [quality-performance-audit-2026-08-20.md](../audits/quality-performance-audit-2026-08-20.md). Epic [#50](https://github.com/The-Allsparks/AMPER/issues/50) **closed** ([#60](https://github.com/The-Allsparks/AMPER/pull/60)–[#71](https://github.com/The-Allsparks/AMPER/pull/71)). Does **not** outrank [#41](https://github.com/The-Allsparks/AMPER/issues/41) or hardware [#6](https://github.com/The-Allsparks/AMPER/issues/6).

| Issue | Priority | Readiness | Dependencies | Status | Next action |
|-------|----------|-----------|--------------|--------|-------------|
| [#50](https://github.com/The-Allsparks/AMPER/issues/50) | HIGH | **Done** | none | Closed | — |
| [#51](https://github.com/The-Allsparks/AMPER/issues/51)–[#59](https://github.com/The-Allsparks/AMPER/issues/59) | HIGH–LOW | **Done** | #50 | Closed | — |
| [#31](https://github.com/The-Allsparks/AMPER/issues/31) | MEDIUM | **Done** (`ObservePerformanceBudgetTest` at 4000 samples) | none | Closed ([#75](https://github.com/The-Allsparks/AMPER/pull/75)) | — |

**First implementation/readiness priority:** [#41](https://github.com/The-Allsparks/AMPER/issues/41) (FTC integration reference). Combined-stack acceptance is [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4). Tracking epic for 0.1.x phases remains [#24](https://github.com/The-Allsparks/AMPER/issues/24).

Desktop `./gradlew check` and `sdk-compile` are **not** Control Hub validation. Do not claim FTC-ready from desktop tests, architecture docs, or TeamCode sketches.

## In-flight

No implementation PR is open.

## First readiness epic

| Issue | Priority | Readiness | Dependencies | Status | Blocker | Next action |
|-------|----------|-----------|--------------|--------|---------|-------------|
| [#41](https://github.com/The-Allsparks/AMPER/issues/41) FTC integration reference | HIGH | Software checklist done; Hub + FORGE remain | #6 Hub cost; FORGE#4 combined | Open | Hardware for disabled/passive cost; combined stack external | Run #6 Hub session; see [ftc-integration-checklist.md](ftc-integration-checklist.md) |
| [#42](https://github.com/The-Allsparks/AMPER/issues/42) Ledger/roadmap list #41 first | HIGH | **Done** | none | Closed via [#45](https://github.com/The-Allsparks/AMPER/pull/45) | none | — |
| [#43](https://github.com/The-Allsparks/AMPER/issues/43) Stubs/tools off robot artifacts | HIGH | **Done** | none | Closed via [#46](https://github.com/The-Allsparks/AMPER/pull/46) | none | — |
| [#28](https://github.com/The-Allsparks/AMPER/issues/28) Init vs match lifecycle | MEDIUM | **Done** | none | Closed | none | — |
| [#25](https://github.com/The-Allsparks/AMPER/issues/25) Protect `main` | HIGH | **Done** | none | Closed | none | — |
| [#44](https://github.com/The-Allsparks/AMPER/issues/44) Sibling electrical contracts | MEDIUM | **Done** | none | Closed | none | — |
| [#6](https://github.com/The-Allsparks/AMPER/issues/6) Hub characterization | HIGH | **Blocked** | Control Hub | Open | Hardware unavailable | Human robot session; #41 consumes these numbers |

## Existing phase issues

| Issue | Priority | Readiness | Dependencies | Status | Blocker | Next action |
|-------|----------|-----------|--------------|--------|---------|-------------|
| [#1](https://github.com/The-Allsparks/AMPER/issues/1) Research | MEDIUM | **Done** (living docs) | none | Closed | none | — |
| [#2](https://github.com/The-Allsparks/AMPER/issues/2) Phase 0 abstraction | MEDIUM | **Software done** | none | Closed; Hub on #6 | none | — |
| [#3](https://github.com/The-Allsparks/AMPER/issues/3) REV adapter | MEDIUM | **Software done** | none | Closed; Hub on #6 | none | — |
| [#4](https://github.com/The-Allsparks/AMPER/issues/4) Logging | MEDIUM | **Software done** | none | Closed; Hub retrieve on #6 | none | — |
| [#5](https://github.com/The-Allsparks/AMPER/issues/5) Phase 1 | MEDIUM | **Software done** | none | Closed; Hub on #6 | none | — |
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
| R1 | [#25](https://github.com/The-Allsparks/AMPER/issues/25) Protect `main` | HIGH | **Done** | none | Closed after policy docs + `sdk-compile` required; review count 0 by solo-maintainer decision | — |
| Dep1 | [#29](https://github.com/The-Allsparks/AMPER/issues/29) Gradle/JUnit majors | HIGH | **Done** (Gradle 9.7 analyzed) | none | Closed ([#72](https://github.com/The-Allsparks/AMPER/pull/72)) | JUnit 6 still rejected |
| A3 / S5 | [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection flag gate | HIGH | **Software done** (session gate + example) | none for the seam | Open until Hub enable decision | Keep default-off; do not claim competition-ready |
| C6 / S6 | [#27](https://github.com/The-Allsparks/AMPER/issues/27) Gravity hold direction | HIGH | **Blocked** | #6, #26, #7 | Open | Do not implement actuation |
| C1 | [#28](https://github.com/The-Allsparks/AMPER/issues/28) `observe()` match reset | MEDIUM | **Done** | none | Closed | — |
| P1 | [#6](https://github.com/The-Allsparks/AMPER/issues/6) Hub overhead | HIGH | **Blocked** | Hardware | Open | Hardware |
| P2 / P3 | [#31](https://github.com/The-Allsparks/AMPER/issues/31) Logger/allocation baseline | MEDIUM | **Done** | none | Closed ([#75](https://github.com/The-Allsparks/AMPER/pull/75)) | — |
| Dep2 | [#30](https://github.com/The-Allsparks/AMPER/issues/30) Pin Actions SHAs | MEDIUM | **Done** ([#73](https://github.com/The-Allsparks/AMPER/pull/73)) | none | Closed | SHA pins with version comments |
| U2 / D1 / D3 | Doc contradictions | MEDIUM | **Done** in PR #18 | none | Merged | — |
| D5 | [#32](https://github.com/The-Allsparks/AMPER/issues/32) SECURITY contact | LOW | **Done** ([#74](https://github.com/The-Allsparks/AMPER/pull/74)) | none | Closed | Private Vulnerability Reporting |

## Stop conditions currently true

- Hardware for #6 is unavailable in this environment.
- Automatic merge is **not** authorized.
- Phase 2+ actuation is behind a readiness gate. Do not start #7–#16 because #41 exists.
- Do not merge unanalyzed JUnit 6 Dependabot majors.
- Do not claim FTC-ready from desktop tests, `sdk-compile`, or this ledger.
