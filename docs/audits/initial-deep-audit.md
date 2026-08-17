# Initial deep audit — AMPER

| Field | Value |
|-------|-------|
| **Date** | 2026-08-17 |
| **Audited commit** | `794a8e36d87a947141eccd6681c5807c26a02c85` (`release/passive-0.1.0-rc.1`) |
| **Default branch SHA** | `519f0a416428bf37c3b8b4d20ddaaee8a1fa2fd8` (`main`, post-#17 scaffold only) |
| **Auditor identity** | `TA-C-GHill` |
| **Hardware validation** | None. Desktop tests and simulated traces only. |

This audit inspects the **0.1.0-rc.1 product** on open PR [#18](https://github.com/The-Allsparks/AMPER/pull/18), not only `main`. `main` does not yet contain the installable FTC library.

---

## Executive summary

AMPER is a **passive-first FTC electrical-awareness library**. The 0.1.0-rc.1 software on PR #18 matches that purpose: teams can measure hub voltage, optionally observe selected motors, warn, log, and export AdvantageScope CSV **without commanding motors**.

The implementation is **software-complete for Phase 0/1** and **not Control Hub validated**. Later phases exist as disabled foundations or stubs. That is consistent with the project’s readiness gate: observe before limiting power.

There is **no BLOCKER** that prevents continued passive development. There is **no evidence** of a Phase 0/1 path that writes `setPower` / `setVelocity`.

A second read-only pass confirmed that verdict and added Phase 0/1 defects: stall dwell vs `SKIPPED` current (#33), silent `publishTelemetry` when Phase 1 is off (#34), latched weak-battery warnings (#35), and stub-only SDK compile in CI (#36).

Highest-value remaining work is: **merge PR #18 after human approval**, then **#34** (DS telemetry when Phase 1 is off), then **#33** / **#35**. Hardware remains **#6**. Do not enable Phases 3–7.

---

## Project purpose

AMPER gives FTC robots situational awareness of their **electrical system**, complementary to [ViDAR](https://github.com/The-Allsparks/ViDAR) field awareness.

Intended users: Allsparks students, other FTC teams adopting one feature at a time, and mentors who must disable everything quickly.

Explicit responsibilities: measurement, validity, logging, driver warnings, later **optional** demand limiting.

Not AMPER’s job: PID/feedforward, mechanism safety, vision, pathing, radio/DS link health, match strategy, or replacing Hub firmware brownout behavior.

---

## Current maturity

| Area | Maturity |
|------|----------|
| Version label | `0.1.0-rc.1` prerelease |
| Phase 0 | Software implemented and unit-tested. **Not Hub-validated.** |
| Phase 1 | Software implemented; default flags keep it off; examples enable `AmperPolicies.passiveDefaults()`. **Not Hub-validated.** |
| Phase 2 | Experimental, opt-in, **not wired into `AmperSession`**. Disabled by default. |
| Phase 3 | State machine exists; **not called from `AmperSession`**. Reports state only. |
| Phases 4–7 | Stubs / not implemented. Pass-through grants. Shadow recorder only for Phase 5. |
| SystemCore | Boundary type only. Issue #16 blocked. |
| Hardware | [docs/validation/STATUS.md](../validation/STATUS.md): **not yet run** |
| Release | PR #18 ready for review, CI green. No git tag. No GitHub Packages publish. |

---

## Implemented capabilities

- Pure-Java `amper-core` monitor, filters, validity, sampling, session lifecycle, bounded logs.
- FTC adapters in `amper-ftc`: `AmperFtc.builder(hardwareMap)`, named voltage discovery, observation-only `DcMotorEx` adapter, `/sdcard/FIRST/amper/` export.
- Phase 1: mechanism start/stop, stall **suspicion**, rate-limited driver states, match summary.
- AdvantageScope table/list CSV, schema sidecar, desktop WPILOG converter (`amper-tools`).
- Compile-checked example OpModes, including characterization.
- Architecture guards: core must not import FTC/Android; FTC spy motors fail if outputs are written.
- Feature flags default intervention **off**. `AmperPolicies.disabled()` skips hardware reads.

## Documented but unimplemented capabilities

| Capability | Evidence |
|------------|----------|
| Phase 2 applied in the session loop | `LocalProtection` is subsystem-owned; `AmperSession` never applies it |
| Phase 3 intervention | `VoltageStateMachine` used only in its unit tests |
| Phase 4 allocation | `PowerCoordinator.allocate` always returns unrestricted grants |
| Phase 5 predictive model | `ShadowSagPredictor` records only; no \(R_{eff}\) actuation |
| Phase 6 load shaping | No implementation (issue #14) |
| Phase 7 adaptive profiles | No implementation (issue #15) |
| SystemCore APIs | `SystemCoreAdapterBoundary` only (issue #16) |
| On-robot native WPILOG | Explicitly not implemented; CSV is the robot format |
| Total battery current | `readBatteryCurrent` remains unsupported in REV adapter |
| Competition-validated thresholds | `ThresholdProvenance.CONSERVATIVE_PLACEHOLDER` |

---

## Architecture findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| A1 | INFORMATIONAL | ARCHITECTURE | Module split is sound: `amper-core` → no FTC types (enforced by `PassiveArchitectureTest`); `amper-ftc` adapters; stubs CI-only; tools desktop-only. |
| A2 | INFORMATIONAL | ARCHITECTURE | `AmperSession` is the OpMode façade (monitor, loggers, trackers). Large but still observation-only. Acceptable for 0.1.x. |
| A3 | HIGH | SAFETY | `AmperFeatureFlags.phase2LocalProtection` does not gate `LocalProtection.apply()`. Subsystems can constrain motors with `LocalProtection.builder().enabled(true)` while session flags remain off. Opt-in is explicit, but the session flag is currently a non-functional kill switch. Tracked as a Phase 2 seam, not a Phase 0/1 bypass. |
| A4 | MEDIUM | ARCHITECTURE | `VoltageStateMachine` is dead code in the robot path. Fine for Phase 3 foundation; do not advertise it as integrated. |
| A5 | LOW | ARCHITECTURE | `PowerCoordinator` ignores its own `intervene` branch and always pass-throughs. Honest stub; keep it that way until Phase 4. |
| A6 | INFORMATIONAL | ARCHITECTURE | No compile-time coupling to ViDAR, Pedro, MIMIC, BEACON, TRACE, or HELM. Docs only mention ViDAR. Correct dependency direction. |
| A7 | LOW | DOCUMENTATION | Convention docs said Java 11; install/build are Java 8. **Fixed in PR #18** (`3231ce5`). |

---

## Correctness findings

| ID | Sev | Type | Finding | Evidence |
|----|-----|------|---------|----------|
| C1 | MEDIUM | CORRECTNESS | `observe()` auto-promotes `INITIALIZED`/`STOPPED` → `STARTED` **without** `resetMatch()`. Teams who sample in `init`/`init_loop` and omit `OpMode.start()` mix init samples into match summaries and CSV. The example path calls `start()`, which does reset. | `AmperSession.observe()` vs `start()` |
| C2 | LOW | CORRECTNESS | `publishCanonical` scans the entire `logger.snapshot()` every loop to attach the last same-timestamp event. Correct but quadratic in logger size as capacity fills. | `AmperSession.publishCanonical` |
| C3 | LOW | CORRECTNESS | `CanonicalLog.append` clamps backward timestamps instead of dropping the sample. Preserves monotonicity for AdvantageScope; can hide clock bugs. Documented by `backwardTimestamps` counter. | `CanonicalLog.java` |
| C4 | INFORMATIONAL | CORRECTNESS | Missing voltage is `NaN` + `MISSING`, not 0. Covered by `AmperLifecycleAndSafetyTest.missingVoltageIsNotValidAndNotZero`. | |
| C5 | INFORMATIONAL | CORRECTNESS | Duplicate `observe()` inside the configured window does not resample. Tested. | |
| C6 | MEDIUM | CORRECTNESS | `GravityHoldPolicy.enforce(allowed, requestedSign)` uses request sign; a `0` request becomes **positive** hold (`sign = +1`). Wrong-direction hold is possible if a team enables Phase 2 gravity policy without a declared hold direction. Not reachable from Phase 0/1. | `GravityHoldPolicy.java` lines 42–48; test expects `enforce(0.0, 1.0) == 0.15` |
| C7 | HIGH | CORRECTNESS | Stall dwell clears when current is `SKIPPED`. Round-robin `recommended()` sampling therefore cannot accumulate multi-motor stall warnings. | `StallSuspicionTracker.looksStalled`; `CurrentSample.isUsable`; issue #33 |
| C8 | HIGH | CORRECTNESS | Weak-battery classification uses whole-match max−min filtered voltage, so one sag can latch `SUSPECTED_WEAK_BATTERY` for the rest of the match. | `BatteryEstimator`; `DriverFeedback.classify`; issue #35 |

Do not treat placeholder voltage thresholds as defects. They are labeled `CONSERVATIVE_PLACEHOLDER`.

---

## Safety findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| S1 | INFORMATIONAL | SAFETY | Phase 0/1 cannot command hardware. `FtcMotorTelemetry` only calls `getPower` / `getCurrent` / `getVelocity` / `getCurrentPosition`. Spy test fails on `setPower`/`setVelocity`. Core source scan forbids those strings except `sim/`. |
| S2 | INFORMATIONAL | SAFETY | `AmperPolicies.disabled()` performs no voltage reads. Tested. |
| S3 | INFORMATIONAL | SAFETY | Exceptions in adapters become `MISSING`/`UNSUPPORTED`, not invented zeros. |
| S4 | INFORMATIONAL | SAFETY | `VoltageStateMachine.interventionPermitted` is false in `SENSOR_FAULT`. Unused on robot until Phase 3 is wired. |
| S5 | HIGH | SAFETY | Same as A3: Phase 2 local protection is not session-flag gated. **Do not enable on a robot until #6 characterization and an explicit flag gate exist.** |
| S6 | HIGH | SAFETY | Same as C6: gravity hold sign inferred from request, including zero. Phase 2 blocked. |
| S7 | INFORMATIONAL | SAFETY | Replay/CSV tools do not instantiate FTC motors. `CsvReplay` / `amper-tools` are desktop. |
| S8 | LOW | SAFETY | `PassiveArchitectureTest` excludes `sim/` and does not scan `amper-ftc`. FTC write protection is the spy test. `SimulatedMotor` javadoc **fixed** in `3231ce5`. |

No path was found that energizes motors from logging, replay, or Phase 0/1 observe.

---

## Performance findings

All items below are **predicted**, not measured on a Control Hub.

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| P1 | HIGH | PERFORMANCE | Hub current-poll cost is unknown. `SamplingPolicy.recommended()` already limits to one current read per loop. Issue #6 must measure this. |
| P2 | MEDIUM | PERFORMANCE | `PowerEventLogger` and `CanonicalLog` use `ArrayList.remove(0)` when full (default 4000). O(n) copy per overflow plus per-loop `LinkedHashMap` field maps. |
| P3 | MEDIUM | PERFORMANCE | `publishCanonical` iterates the whole event snapshot each loop (C2). |
| P4 | LOW | PERFORMANCE | `observe()` allocates lists of voltage samples and motor snapshots every cycle. Typical for this style of Java 8 library; still worth a desktop allocation baseline. |
| P5 | INFORMATIONAL | PERFORMANCE | CSV flush happens in `stop()`, not in the control loop. Correct. |
| P6 | INFORMATIONAL | PERFORMANCE | No thread pool. Session is single-threaded and caller-driven. Good for FTC OpModes. |

Create a **desktop** microbench issue; do not invent Hub numbers.

---

## API / usability findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| U1 | MEDIUM | USABILITY | README / examples enable Phase 1 via `passiveDefaults()`, while `AmperFeatureFlags.defaults()` keeps Phase 1 off. Documented, but first-use copies the example (Phase 1 on). Acceptable if warnings stay advisory. |
| U2 | LOW | USABILITY | Quickstart init telemetry wording. **Fixed in PR #18** (`3231ce5`). |
| U3 | LOW | USABILITY | Expansion Hub voltage requires an exact configured name. Correct (no silent guess); error lists available names. |
| U4 | INFORMATIONAL | USABILITY | Disable paths exist: `disabled()`, `measurementOnly()`, `persistLogs(false)`. |
| U5 | LOW | USABILITY | Public surface is wider than the student path (`PowerCoordinator`, `VoltageStateMachine`, `LocalProtection`). Javadoc marks them experimental; examples do not use them. |
| U6 | HIGH | USABILITY | `publishTelemetry` stays silent after the first `observe()` when Phase 1 is off or AMPER is disabled, despite javadoc promising a one-line state (#34). |

---

## Testing findings

| Area | Coverage |
|------|----------|
| Public Phase 0/1 session | `AmperSessionTest`, lifecycle/safety tests |
| Validity / missing / unsupported | `ExceptionAndMissingCurrentTest`, FTC integration tests |
| Multi-hub | `MultiHubMonitorTest`; FTC named/ambiguous hub tests |
| Architecture guards | `PassiveArchitectureTest`; FTC spy motor |
| Logging / AdvantageScope / WPILOG | compatibility + type-stability tests; CI fixture artifact |
| Phase 2/3 units | `LocalProtectionTest`, `VoltageStateMachineTest` (not session-wired) |
| Docs links | `DocLinkCheckerTest` |
| Examples | compile only (`amper-examples`) |

Gaps:

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| T1 | MEDIUM | TESTING | No test that `init`/`init_loop` observes plus omitted `start()` mix match stats (C1). |
| T2 | MEDIUM | TESTING | No test that `LocalProtection` ignores or honors `AmperFeatureFlags`. |
| T3 | LOW | TESTING | No concurrency tests. Acceptable: API is single-threaded. |
| T4 | INFORMATIONAL | TESTING | `javadoc` has `failOnError = false`. Javadoc is not a correctness gate. |
| T5 | INFORMATIONAL | TESTING | Hardware procedures exist but have never been executed. |

---

## Documentation findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| D1 | MEDIUM | DOCUMENTATION | `docs/power-management/conventions.md` and `risks.md` still say Java 11; install/build are Java 8. |
| D2 | MEDIUM | DOCUMENTATION | Quickstart init telemetry (U2). |
| D3 | LOW | DOCUMENTATION | `SimulatedMotor` javadoc contradicts `setVelocity`. |
| D4 | INFORMATIONAL | DOCUMENTATION | Research, phases, architecture, install, logging, validation card, and issue matrix are unusually complete for a 0.1 rc. |
| D5 | LOW | DOCUMENTATION | `SECURITY.md` has no published private email; GitHub advisories are the only concrete channel. |
| D6 | INFORMATIONAL | DOCUMENTATION | Issue bodies use the older phase-work template, not the full orchestrator template. Existing #1–#16 remain valid. |

---

## Dependency findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| Dep1 | HIGH | COMPATIBILITY | Dependabot opened Gradle wrapper **9.7.0** (#21) and JUnit BOM **6.1.3** (#20). Both **failed CI**. Do not merge. FTC SDK 11.2 TeamCode uses Gradle 9.1; AMPER **itself** currently builds with wrapper **8.7**. Keep AMPER’s wrapper until a compatibility analysis exists. |
| Dep2 | MEDIUM | SECURITY | GitHub Actions use floating majors (`actions/checkout@v4`, `setup-java@v4`, `upload-artifact@v4`). Dependabot PRs #22/#23 bump checkout 4→7 and setup-java 4→5; CI green but unreviewed. Prefer SHA pins. |
| Dep3 | INFORMATIONAL | COMPATIBILITY | Library bytecode Java 8, CI Temurin 17: matches FTC SDK 11.2 `build.common.gradle`. |
| Dep4 | HIGH | COMPATIBILITY | `ftcSdkVersion = '11.2.0'` is unused; `amper-ftc` compiles only against stubs. CI cannot catch SDK drift. Issue #36. |
| Dep5 | INFORMATIONAL | SECURITY | No secrets in repo. Release workflow does not publish packages yet. MIT license compatible with ViDAR. |
| Dep6 | LOW | COMPATIBILITY | Dependabot Gradle updates are not grouped to minor/patch, unlike Actions. That is why majors opened. |

---

## Repository-health findings

| ID | Sev | Type | Finding |
|----|-----|------|---------|
| R1 | HIGH | SECURITY | `main` has **no branch protection** and **no rulesets**. Admin can merge without review. |
| R2 | HIGH | INTEGRATION | Product lives on draft PR #18, not `main`. `main` is still the Phase 0/1 **scaffold**. |
| R3 | INFORMATIONAL | INTEGRATION | PR #19 (logging) merged into the **release branch**, then into #18. CI on #18 is green. |
| R4 | MEDIUM | USABILITY | Issues #1–#16 lack dependency/blocked-by/acceptance structure required by the orchestrator. They are still the roadmap. |
| R5 | LOW | DOCUMENTATION | No GitHub Project board. Priority lives in docs after this audit. |
| R6 | INFORMATIONAL | INTEGRATION | Open Dependabot PRs #20–#23. #20/#21 failed; #22/#23 succeeded. |
| R7 | INFORMATIONAL | INTEGRATION | No releases/tags. Delete-branch-on-merge is false. Merge commit, squash, and rebase all allowed. |
| R8 | LOW | DOCUMENTATION | `AUTOMATIC_MERGE` is not authorized. Do not merge without a human decision. |

---

## Cross-project integration findings

| Peer | Relationship |
|------|----------------|
| ViDAR | Sibling library. Shared MIT, `main`, public OSS. No code dependency. |
| Pedro Pathing | None in this repo. |
| MIMIC / BEACON / TRACE / HELM | Not referenced in code. Conceptual stack (AMPER below TRACE/HELM) is documentation-only. |
| Robot application | Consumer via `includeBuild` / Maven coordinates. AMPER must not own TeamCode motors. |

No circular dependencies found.

---

## Readiness assessment

**Safe to continue:** yes, for **passive** software and documentation.

**Safe to call Phase 0/1 competition-ready:** **no**. Issue #6 has not run.

**Safe to enable Phase 2+:** **no**. Missing Hub characterization, feature-flag kill switch, and gravity-hold direction.

**Safe to merge PR #18 into `main`:** software/CI yes, after **human approval** (`AUTOMATIC_MERGE=false`). Hardware claims in the PR are already honest.

**Release `v0.1.0-rc.1` tag:** only after #18 is on `main` and maintainers accept software-rc status.

---

## Recommended work order

1. Merge PR #18 after human approval. Do not start a competing implementation PR.
2. [#34](https://github.com/The-Allsparks/AMPER/issues/34) `publishTelemetry` when Phase 1 / AMPER is disabled (first software slice after #18).
3. [#33](https://github.com/The-Allsparks/AMPER/issues/33) stall dwell vs `SKIPPED` current.
4. [#35](https://github.com/The-Allsparks/AMPER/issues/35) weak-battery latch.
5. [#36](https://github.com/The-Allsparks/AMPER/issues/36) compile `amper-ftc` against FTC SDK 11.2 in CI.
6. [#25](https://github.com/The-Allsparks/AMPER/issues/25) branch protection (human).
7. [#29](https://github.com/The-Allsparks/AMPER/issues/29) reject Gradle 9.7 / JUnit 6 majors.
8. [#6](https://github.com/The-Allsparks/AMPER/issues/6) Control Hub characterization (hardware; blocked here).
9. [#26](https://github.com/The-Allsparks/AMPER/issues/26) LocalProtection flag gate (Phase 2 seam).
10. [#27](https://github.com/The-Allsparks/AMPER/issues/27) gravity hold direction (blocked).
11. Phase 3–7 remain behind readiness gates.

---

## Deferred or rejected ideas

| Idea | Decision |
|------|----------|
| Enable Phase 3–7 because stubs exist | Rejected. Readiness gate. |
| Native WPILOG on current Control Hub | Rejected for 0.1.x; CSV is the robot format. |
| Invent SystemCore APIs | Rejected (#16). |
| Merge Gradle 9.7 / JUnit 6 because Dependabot opened PRs | Rejected; CI failed; compatibility unanalyzed. |
| Force-merge #18 | Rejected. No automatic merge authorization; no branch protection to bypass either. |
| Broad rewrite of `AmperSession` | Rejected. Incremental seams are enough. |

---

## Evidence and references

- Repository: [The-Allsparks/AMPER](https://github.com/The-Allsparks/AMPER)
- Draft PR: [#18](https://github.com/The-Allsparks/AMPER/pull/18)
- Merged: [#17](https://github.com/The-Allsparks/AMPER/pull/17), [#19](https://github.com/The-Allsparks/AMPER/pull/19) (into release branch)
- Issue matrix: [issue-matrix.md](../status/issue-matrix.md)
- Priority ledger: [priority-ledger.md](../status/priority-ledger.md)
- Roadmap: [roadmap.md](../status/roadmap.md)
- Key tests: `PassiveArchitectureTest`, `AmperLifecycleAndSafetyTest`, `AmperFtcIntegrationTest`
- CI workflow: `.github/workflows/ci.yml` (`./gradlew check javadocAll assembleReleaseArtifacts`)

Finding IDs in this document map to GitHub issues **#24–#36**. Existing issues #1–#16 are **not** duplicated.
