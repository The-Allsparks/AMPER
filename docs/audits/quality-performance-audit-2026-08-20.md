# Quality, performance, and CI audit — AMPER

| Field | Value |
|-------|-------|
| **Date** | 2026-08-20 |
| **Audited commit** | `4a6d185` (`main`, post `#49`) |
| **Hardware validation** | None. Desktop `./gradlew check` and `compileAgainstFtcSdk` only. |
| **Related prior audit** | [initial-deep-audit.md](initial-deep-audit.md) (2026-08-17 / follow-up 2026-08-18) |

This audit does **not** replace the 2026-08-17 safety verdict. Phase 0/1 remains observation-only. This pass scores structure, Java/FTC quality, performance readiness, and CI enforcement, then records issues and the checks added afterward.

Desktop `check`: **PASS**. `compileAgainstFtcSdk`: **PASS**.

---

## Overall structural score: **72 / 100**

| Category | Score | Evidence | Strengths | Weaknesses | Most important improvement |
|----------|------:|----------|-----------|------------|----------------------------|
| Architecture and boundaries | 16 / 20 | Five Gradle modules; `PassiveArchitectureTest`; `assertRobotFacingArtifacts`; documented map in `docs/power-management/architecture.md` | Core has no FTC/Android types; adapters are thin; composition root is `AmperSession` | Package rules were documentation-only; `AmperSession` is the god façade (~416 lines); experimental Phase 2–5 types live in the same public packages as student APIs; `PowerMonitor` depends on `PowerPolicy` | Keep enforcing package direction in tests (added this pass) |
| Maintainability and readability | 11 / 15 | Java 8 explicit style; almost no TODOs; honest javadoc on experimental types | Names match electrical vocabulary; presets in `AmperPolicies` | `PowerPolicy` builder is large (~324 lines); dual log models (`PowerEventLogger` + `CanonicalLog`) duplicate per-loop work; AI-narration is rare | Do not split `AmperSession` until characterization tests exist for lifecycle |
| Dependency and state management | 12 / 15 | No static mutable state; no executors; `AmperClock` injected; lifecycle enum | Fail-safe missing sensors; session flag kill switch for Phase 2 | `observe()` still auto-`initialize()`; boxed `Double` caches in `PowerMonitor`; catch-all `RuntimeException` in adapters (intentional, but hides bugs) | Keep hardware on the OpMode thread |
| Testing and testability | 12 / 15 | 20+ test classes covering session, validity, FTC spy motors, logs, architecture | Clocks and suppliers make core unit-testable | Examples compile-only; javadoc not a gate; no allocation profiler; hardware never run | Keep adding characterization tests before refactors |
| Build and repository hygiene | 8 / 10 | Wrapper 8.7; Java 8; Dependabot; robot artifact assertion | SDK compile job exists | Floating Action majors (#30); ungrouped Gradle majors (#29); no version catalog | Group Dependabot Gradle to minor/patch |
| Static-analysis coverage | 5 / 10 | `-Xlint:unchecked,deprecation`; source architecture scanners (expanded this pass) | Custom rules match FTC constraints better than a generic linter | No Spotless/PMD/SpotBugs; no `-Werror`; ArchUnit not used | Keep source scanners; do not dump Spotless into this tree without a format PR |
| Documentation and contributor guidance | 4 / 5 | Unusually complete docs for a 0.1 rc | Honest maturity labels | `AGENTS.md` was missing before this pass | Point agents at `AGENTS.md` + this standard |
| CI enforcement | 8 / 10 | Ubuntu+Windows `check`; `sdk-compile`; `docs-structure`; branch protection requires those four jobs | `enforce_admins`; no fake hardware job | Architecture lived only inside `check`; no CodeQL; no performance budget until this pass | Keep architecture/budget tests inside `check` so existing required jobs enforce them |

A passing build is not by itself good architecture. The module split is the strongest structural asset; missing executable package rules and hot-path guards were the largest CI gaps.

---

## Performance score: **60 / 100** — functional, performance debt exists

No Control Hub profiler traces exist. Scores below use **STRONG STATIC EVIDENCE** unless marked MEASURED (desktop only).

| Category | Score | Evidence | Weak points | Likely impact | Confidence | Recommended measurement |
|----------|------:|----------|-------------|---------------|------------|-------------------------|
| Algorithmic efficiency | 12 / 20 | Round-robin current; bounded capacity; `publishCanonical` scans `logger.snapshot()` each loop | `ArrayList.remove(0)` on overflow; O(n) snapshot copy + scan; dual log append | Jitter once the 4000-sample log fills | High (static) | Desktop overflow bench (#31); Hub loop histogram (#6) |
| Allocation / GC | 8 / 20 | `PowerMonitor.update` allocates lists, `boolean[]`, boxed map values, anonymous `DoubleRead`; `PowerEventLogger.recordObservation` builds a `LinkedHashMap` and `String.format`s; `ElectricalObservation` copies lists; `CanonicalSample` copies maps | GC on Control Hub during teleop | Medium–high on 4–8 motors | High (static), none on Hub | Allocation count around `observe()` on desktop; Hub GC if available |
| Critical-path latency | 11 / 20 | `LoopOverheadStats` records min/mean/max + window percentiles; CSV flush is in `stop()` | DS telemetry publishes mean-ish loop µs only, not p95/p99; default `PowerPolicy.builder()` uses `SamplingPolicy.everyLoop()` | Unpredictable loop time if a team uses `PowerPolicy.defaults()` instead of `AmperPolicies.passiveDefaults()` | Medium | Student presets are now CI-locked to 1 current read/loop |
| Hardware / I/O | 12 / 15 | Adapters catch SDK exceptions; recommended sampling is 1 current/loop; file export after match | Hub current poll cost unknown (#6, #3) | Dominant if I2C is expensive | Low until Hub | Hardware test card |
| Concurrency | 9 / 10 | No worker threads, no locks, OpMode-driven | None material | Low | High | Do not add executors |
| Observability | 7 / 10 | `PERFORMANCE_UPDATE_DURATION_SECONDS` in canonical log; match summary max/mean loop | p50/p95/p99 not on DS or match summary | Teams cannot see tail latency | High | Publish `LoopOverheadStats` percentiles |
| Regression protection | 3 / 5 | Capacity test existed; this pass adds a generous desktop budget and relative slowdown ceiling | Not a Hub SLA | Catastrophic O(n²) blow-ups only | MEASURED (desktop) | Keep the loose budget; do not tighten to milliseconds |

**Classification:** 60–69, functional but performance debt exists.

---

## Highest-risk structural problems

1. Package boundaries were not executable (addressed in CI this pass; remaining: public experimental types).
2. `AmperSession` owns lifecycle, logging, telemetry, and Phase 2 helpers — acceptable for 0.1.x, risky to grow.
3. `PowerMonitor` depends on full `PowerPolicy` rather than a sampling/threshold value type.
4. Dual log pipelines on every started `observe()`.

## Highest-risk performance problems

1. **STRONG STATIC EVIDENCE** — per-loop allocations in `PowerMonitor.update`, `ElectricalObservation`, `PowerEventLogger.recordObservation`, `CanonicalLog.append`.
2. **STRONG STATIC EVIDENCE** — `ArrayList.remove(0)` overflow in both logs (issue #31 parent).
3. **STRONG STATIC EVIDENCE** — `publishCanonical` copies and scans the event log every loop.
4. **SUSPECTED** — Hub current-read cost (issue #6). Do not treat desktop ns/observe as Hub truth.
5. **MEASURED (desktop only)** — `ObservePerformanceBudgetTest` prints warmup vs later ns/observe. It is not a Hub number.

## Findings that CI cannot reliably test

| Finding | Instead |
|---------|---------|
| Control Hub loop time, I2C, GC | Hardware card + `LoopOverheadStats` on robot (#6) |
| Whether `remove(0)` is material at 4000 samples on Dalvik/ART | Hub trace; desktop bench is indicative only |
| Vision / camera | N/A (AMPER has no vision pipeline) |
| Exact millisecond SLAs | Forbidden on GitHub-hosted runners |

## GitHub issues created

Parent epic: [#50](https://github.com/The-Allsparks/AMPER/issues/50). Did not duplicate #31, #30, #29, #6, or #41.

| Issue | Priority | Category | Problem | CI detectable? |
|-------|----------|----------|---------|----------------|
| [#50](https://github.com/The-Allsparks/AMPER/issues/50) | P1 epic | architecture / CI | Quality baseline + executable standards | Partial (docs + tests landed) |
| [#51](https://github.com/The-Allsparks/AMPER/issues/51) | P1 | performance | Per-loop allocations in `observe()` | Partial (hot-path guards; not allocation counts) |
| [#52](https://github.com/The-Allsparks/AMPER/issues/52) | P2 | performance | `ArrayList.remove(0)` overflow | After fix: source scan possible |
| [#53](https://github.com/The-Allsparks/AMPER/issues/53) | P2 | performance | Full `logger.snapshot()` scan each loop | After fix: unit test |
| [#54](https://github.com/The-Allsparks/AMPER/issues/54) | P2 | performance | Loop percentiles not on DS/summary | After fix: session tests |
| [#55](https://github.com/The-Allsparks/AMPER/issues/55) | P2 | performance / usability | `PowerPolicy.defaults()` uses `everyLoop()` current | Yes (extend preset test) |
| [#56](https://github.com/The-Allsparks/AMPER/issues/56) | P3 | CI | Compiler-warning ratchet | After enablement |
| [#57](https://github.com/The-Allsparks/AMPER/issues/57) | P3 | architecture | Experimental types look public | Docs only |
| [#58](https://github.com/The-Allsparks/AMPER/issues/58) | P3 | CI | Spotless in a format-only PR | After format PR |
| [#59](https://github.com/The-Allsparks/AMPER/issues/59) | P3 | CI | Require CodeQL only after green `main` | Ops |

## CI coverage added in this pass

Recorded in [quality-standards.md](../architecture/quality-standards.md). Application refactors remain issue-driven and were **not** done in this pass.

Recommended order **among these children** (does not outrank [#41](https://github.com/The-Allsparks/AMPER/issues/41) or [#6](https://github.com/The-Allsparks/AMPER/issues/6)): **#51**, then **#53**, then **#55**.
