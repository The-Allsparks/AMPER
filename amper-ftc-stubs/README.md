# amper-ftc-stubs

Compile-only stand-ins for FTC SDK and Android types so this repository can
`compileJava` and run JVM unit tests without the Android Gradle Plugin.

**Do not** add this module to a TeamCode project. Robot builds must compile
`amper-ftc` against the official
[`FtcRobotController`](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
SDK (`org.firstinspires.ftc:RobotCore` / `Hardware`).

CI also compiles the adapters against RobotCore 11.2.0
(`./gradlew compileAgainstFtcSdk`). This module remains the default
desktop classpath and is **not** published.
