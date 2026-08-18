# Installing AMPER in an FTC SDK project

AMPER is a Java library. It does **not** replace `FtcRobotController`. You add it to a current-season [FtcRobotController](https://github.com/FIRST-Tech-Challenge/FtcRobotController) TeamCode module.

Verified against **FTC SDK 11.2.0** (DECODE 2025-2026) on **2026-08-17**:

| Item | Value | Source |
|------|--------|--------|
| Maven coordinates | `org.firstinspires.ftc:RobotCore:11.2.0` (and Hardware, FtcCommon, …) | [FtcRobotController v11.2 `build.dependencies.gradle`](https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v11.2/build.dependencies.gradle) |
| TeamCode Java language | `sourceCompatibility` / `targetCompatibility` **1.8** | [v11.2 `build.common.gradle`](https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v11.2/build.common.gradle) |
| AMPER bytecode | Java 8 | this repository |
| Gradle used to *build AMPER itself* | 8.7 wrapper | `gradle/wrapper/gradle-wrapper.properties` |
| Gradle used to *build TeamCode* | the SDK's wrapper (v11.2 uses Gradle 9.1 / AGP 8.13.2) | [FtcRobotController README v11.2](https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v11.2/README.md) |

Do not upgrade the FTC project's Gradle because AMPER asked you to. Keep the SDK wrapper.

## Method A — Gradle composite build (recommended while AMPER is a prerelease)

1. Clone AMPER next to your FTC project (or anywhere you can point to):

```text
Documents/
  FtcRobotController/
  AMPER/
```

2. In the FTC project's `settings.gradle` (or `settings.gradle.kts`) add:

```gradle
includeBuild('../AMPER')
```

3. In `TeamCode/build.gradle` add:

```gradle
dependencies {
    implementation 'org.allsparks:amper-core:0.1.0-rc.1'
    implementation 'org.allsparks:amper-ftc:0.1.0-rc.1'
}
```

`amper-ftc` already depends on `amper-core`. Keep both coordinates so the FTC adapters are on the classpath.

4. Sync Android Studio, then continue with [quickstart.md](quickstart.md).

This compiles AMPER from source with your project. No Maven login is required.

## Method B — JitPack (after a git tag exists)

When maintainers publish a git tag such as `v0.1.0-rc.1`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.The-Allsparks.AMPER:amper-core:v0.1.0-rc.1'
    implementation 'com.github.The-Allsparks.AMPER:amper-ftc:v0.1.0-rc.1'
}
```

Until a tag is published, use Method A. JitPack coordinates are documented here so teams do not copy unexplained source trees as the primary install path.

AMPER's repository CI also compiles `amper-ftc` against `org.firstinspires.ftc:RobotCore:11.2.0` from Maven Central. TeamCode does not need that job; your FTC project already has RobotCore on the classpath. Local `./gradlew check` in this repo still uses `amper-ftc-stubs` and does not need the Android Gradle Plugin.

## Method C — GitHub Packages (optional maintainer publish)

See [release.md](release.md). Not required for the first student install.

## Copying source is a fallback only

Copying `amper-core` / `amper-ftc` Java into TeamCode is a **temporary fallback** if Gradle composite build is impossible. If you do that, copy both modules, keep the `org.allsparks.amper` packages, and do **not** copy `amper-ftc-stubs` (those exist only so this repository can compile without Android).

## After install

Follow [quickstart.md](quickstart.md). Example OpModes live in [`amper-examples`](../amper-examples) and are described in [`examples/README.md`](../examples/README.md).
