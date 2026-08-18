# Release process

AMPER follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html) and [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## Version meaning

| Label | Meaning |
|-------|---------|
| `0.1.0-rc.1` | First **software** release candidate for passive Phase 0/1. Hardware validation outstanding. |
| `0.1.0` | Same software gates **plus** documented Control Hub characterization in `docs/validation/` |
| `1.x` | Compatibility commitments after teams have used the library in season |

Do not cut `0.1.0` final until [docs/validation/STATUS.md](validation/STATUS.md) is no longer `not yet run` for the required Phase 0/1 measurements.

## Checklist before tagging

- [ ] `./gradlew check javadocAll assembleReleaseArtifacts`
- [ ] Example OpModes compile (`:amper-examples:compileJava`)
- [ ] Intervention flags default false (unit tests)
- [ ] Phase 0/1 tests prove no `setPower` / `setVelocity`
- [ ] README / issue matrix match implementation
- [ ] Changelog updated
- [ ] No SystemCore behavior invented
- [ ] No Phase 3–7 feature enabled by default
- [ ] Hardware status remains honest

## Tagging

```bash
git tag v0.1.0-rc.1
git push origin v0.1.0-rc.1
```

`.github/workflows/release.yml` builds jars. Publishing to GitHub Packages or Maven Central is a maintainer step after secrets exist. JitPack can build from the tag without extra credentials.

## Artifacts

| Artifact | Contents |
|----------|----------|
| `amper-core` | Pure Java. No Android / FTC SDK |
| `amper-ftc` | FTC adapters. Compile against official SDK on the robot |
| sources / javadoc jars | Gradle `withSourcesJar()` / `withJavadocJar()` |
| `amper-ftc-stubs` | **Not published.** CI compile stand-ins only |
