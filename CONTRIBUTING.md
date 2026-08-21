# Contributing to AMPER

AMPER is maintained by [The Allsparks](https://github.com/The-Allsparks) (FTC Team 36117) for our team and the wider FTC community.

## Setup

```powershell
git clone https://github.com/The-Allsparks/AMPER.git
cd AMPER
.\gradlew.bat check
.\gradlew.bat compileAgainstFtcSdk
```

Install into TeamCode: [docs/install.md](docs/install.md).

Coding agents: read [AGENTS.md](AGENTS.md) and [docs/architecture/quality-standards.md](docs/architecture/quality-standards.md) before changing module boundaries or the `observe()` path.

## Commands

| Command | What it prevents |
|---------|------------------|
| `.\gradlew.bat check` | Broken tests, architecture regressions, example compile failures, stubs/tools on robot artifacts, format drift (`spotlessCheck`) |
| `.\gradlew.bat spotlessApply` | Rewrite Java (except `amper-ftc-stubs`) to Palantir Java Format |
| `.\gradlew.bat compileAgainstFtcSdk` | Adapters drifting from FTC SDK 11.2.0 |
| `.\gradlew.bat javadocAll assembleReleaseArtifacts` | Missing jars used by the release workflow |

There is no formatter task. Match neighboring Java 8 style. Do not add Spotless in a behavior PR.

## Rules of engagement

1. **Do not enable motor intervention** in PRs without explicit maintainer review and documented acceptance tests.
2. Phase 0 and Phase 1 must remain behavior-neutral for motors.
3. Distinguish **verified fact**, **engineering inference**, and **untested hypothesis** in documentation.
4. Never describe an FRC motor-controller capability as a current FTC capability without evidence.
5. Do not commit secrets, Wi-Fi passwords, tokens, or student PII.

## Pull requests

- Prefer small, reviewable PRs.
- Include motivation, phase impact, test evidence, and safety notes.
- Update docs when behavior or maturity labels change.
- Run `.\gradlew.bat check` (or `./gradlew check`) before requesting review.
- Adapter PRs that touch FTC types should also run `.\gradlew.bat compileAgainstFtcSdk` (CI job `sdk-compile`).
- Architecture tests (`PackageBoundaryTest`, `HotPathGuardTest`, `PassiveArchitectureTest`, `FtcArchitectureTest`) must stay green. If you need a new dependency direction, change the test in the same PR and explain why.
- Do not add `Thread.sleep`, file I/O, networking, or extra threads to `observe()` / `PowerMonitor.update`.
- Do not tighten CI to millisecond-level performance SLAs. Desktop budgets are generous by design.

## Branch protection (`main`)

`main` is protected (classic branch protection; enforce admins on):

| Rule | Current policy |
|------|----------------|
| Force push / delete | Disallowed |
| Required status checks | `test (ubuntu-latest)`, `test (windows-latest)`, `docs-structure`, `sdk-compile`, `Analyze Java` (strict: branch must be up to date) |
| Pull request reviews | Required approving review count is **0** (solo-maintainer workflow). Stale reviews dismiss on new pushes. Conversation resolution required before merge. |
| Admin bypass | Not enabled (`enforce_admins`) |

Do not require hardware jobs that do not exist. Raising the review count above 0 is an org decision when a second reviewer is available. Details: [docs/release.md](docs/release.md).

## GitHub Actions pins

Workflows pin third-party actions to full commit SHAs with a version comment (for example `actions/checkout@<sha> # v7.0.1`). Do not switch back to floating major tags. Dependabot can still open PRs that move those pins.

## Line endings

The repository stores LF line endings (see [.gitattributes](.gitattributes)).

## License

Contributions are accepted under the MIT License ([LICENSE](LICENSE)). No CLA is required.
