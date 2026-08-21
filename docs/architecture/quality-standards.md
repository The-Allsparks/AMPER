# AMPER quality and architecture standards

These rules are enforced by tests where practical. Documentation alone is not architecture.

## Purpose of each automated check

| Check | Prevents |
|-------|----------|
| `./gradlew check` | Broken compile, failing unit tests, examples that do not compile, stubs/tools leaking into robot artifacts |
| `PassiveArchitectureTest` | `amper-core` calling `setPower`/`setVelocity` or importing FTC/Android |
| `PackageBoundaryTest` | Core depending on FTC/Android/`amper-ftc`/`amper-tools`; production → `sim`; measure/filter/clock → intervention/log; policy → hardware |
| `HotPathGuardTest` | `Thread.sleep`, filesystem/network I/O, extra threads, or Stream/Collectors on the observe path |
| `FtcArchitectureTest` | FTC adapters writing motors or depending on desktop tools |
| `ObservePerformanceBudgetTest` | Unbounded logs; student presets polling every motor current; catastrophic desktop superlinear `observe()` cost |
| `LoopOverheadStatsTest` | Broken percentile helpers used for DS `AMPER.p95Us` and match-summary p50/p95/p99 |
| `compileAgainstFtcSdk` | Stub drift from RobotCore 11.2.0 |
| `docs-structure` CI job | Missing required docs |
| `DocLinkCheckerTest` | Broken relative markdown links |

GitHub Actions also runs `check` on Ubuntu and Windows, `sdk-compile`, `docs-structure`, and CodeQL `Analyze Java`.

## Java and Android compatibility

- Ship Java 8 bytecode (`sourceCompatibility` / `targetCompatibility` 1.8).
- CI compiles with Temurin 17, matching FTC SDK 11.2 TeamCode.
- Do not add a JVM-only library to robot modules without an Android/FTC note.
- ArchUnit, Spotless, PMD, and SpotBugs are **not** in this repository. ArchUnit is compatible with `amper-core` JVM tests but duplicates the source scanners above. Spotless would reformat the tree. PMD/SpotBugs would fail hundreds of existing findings without a baseline.

## Compiler warnings

Compile uses `-Xlint:unchecked -Xlint:deprecation`.

| Path | Policy |
|------|--------|
| `amper-core` | `-Werror`. Baseline **0** warnings (2026-08-20). New unchecked/deprecation warnings fail `check`. |
| `amper-ftc` / `amper-examples` / `amper-tools` | Lint flags on; not `-Werror` (FTC stubs and desktop noise). |
| `compileAgainstFtcSdk` | Lint flags on; **not** `-Werror` until RobotCore warnings are enumerated. |
| Javadoc | `failOnError = false`, `Xdoclint:none`. Do not fail the tree on javadoc until a dedicated cleanup. |

## Performance CI policy

Do **not** add gates such as “method must finish in 2.0 ms”. GitHub-hosted runners vary.

Allowed:

- Bounded size (`logger.size() <= capacity`)
- Algorithmic relative slowdown with a large factor
- Generous wall-clock ceilings (tens of seconds)
- Static forbidden APIs on hot paths

Hub loop time, I2C cost, GC pauses, and vision/camera behavior cannot be reproduced here. Measure them on a Control Hub (issue #6). Desktop and DS `LoopOverheadStats` percentiles describe AMPER `observe()` duration only.

## Adding a dependency

1. State why an existing module cannot do the job.
2. Confirm Java 8 + FTC/Android compatibility if the dependency can reach a robot jar.
3. Prefer `testImplementation` for analysis tools.
4. Add a test that would fail if the new dependency were misused (architecture or unit).

## Changing architecture

Update this file, [architecture.md](../power-management/architecture.md), and `AGENTS.md` in the same PR as the boundary change. If a new direction is allowed, add or adjust an architecture test so the old mistake fails CI.
