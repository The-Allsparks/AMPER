# Repository convention assessment (The Allsparks)

Prepared before AMPER creation. AMPER did **not** previously exist under `The-Allsparks/AMPER`.

## Repositories inspected

| Repo | Visibility | Default branch | License | Notes |
|------|------------|----------------|---------|-------|
| [ViDAR](https://github.com/The-Allsparks/ViDAR) | Public | `main` | MIT | Closest product analog (FTC robot library + docs) |
| [ftc-dev-tools](https://github.com/The-Allsparks/ftc-dev-tools) | Public | `main` | Apache-2.0 | Richest governance templates / Dependabot / CoC |
| [ftc-team-analysis](https://github.com/The-Allsparks/ftc-team-analysis) | Public | `main` | MIT | Web tool; lighter robotics conventions |
| SponsorshipPlan | Private | `main` | — | Ignored for OSS library norms |

## Conventions adopted for AMPER

| Topic | Followed from | AMPER choice |
|-------|---------------|--------------|
| Public OSS | ViDAR / tools | Public |
| License | ViDAR (reusable FTC library) | **MIT** (not Apache-2.0) |
| Branch | Org default | `main` |
| Java + Gradle test module | FTC SDK 11.2, not ViDAR `java-pure` | Root `java-library`, **Java 8 bytecode**, CI Temurin 17 |
| LF + `.gitattributes` | ViDAR | Yes |
| CoC / SECURITY / PR template / Dependabot | ftc-dev-tools | Adapted (AMPER safety language) |
| Issue templates | ftc-dev-tools | Bug / feature / phase work |
| Package naming | Mixed | `org.allsparks.amper` (library), documented vs ViDAR `teamcode.vidar` copy path |
| Topics | Requested list | Applied on GitHub |

## Deliberate differences

- **License MIT not Apache-2.0:** match ViDAR as the sibling robot-capability library; Apache remains appropriate for the tooling monorepo.
- **No Python/Docker sim** in the initial scaffold: avoid unnecessary frameworks; Java-first like competition path.
- **Intervention disabled:** stricter than typical feature repos because motors are safety-relevant.
