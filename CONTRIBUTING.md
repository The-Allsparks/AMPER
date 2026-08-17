# Contributing to AMPER

AMPER is maintained by [The Allsparks](https://github.com/The-Allsparks) (FTC Team 36117) for our team and the wider FTC community.

## Setup

```powershell
git clone https://github.com/The-Allsparks/AMPER.git
cd AMPER
.\gradlew.bat test
```

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
- Run `.\gradlew.bat test` (or `./gradlew test`) before requesting review.

## Line endings

The repository stores LF line endings (see [.gitattributes](.gitattributes)).

## License

Contributions are accepted under the MIT License ([LICENSE](LICENSE)). No CLA is required.
