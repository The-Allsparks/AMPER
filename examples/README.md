# Examples

Compile-checked FTC OpModes live in [`amper-examples`](../amper-examples/src/main/java/org/allsparks/amper/examples). Copy a file into TeamCode and remove `@Disabled`. They are **not** Control Hub validated.

| OpMode | Shows |
|--------|--------|
| `AmperVoltageOnlyOpMode` | Voltage + lifecycle + telemetry |
| `AmperVoltageAndCurrentOpMode` | Selected `DcMotorEx` current |
| `AmperLinearOpMode` | LinearOpMode + logging |
| `AmperMultiHubOpMode` | Control Hub + named Expansion Hub |
| `AmperCurrentUnavailableOpMode` | Current polling off |
| `AmperPhase1DisabledOpMode` | Phase 1 off, Phase 0 on |
| `AmperDisabledOpMode` | AMPER disabled |
| `AmperCharacterizationOpMode` | Loop-overhead comparison modes; **do not brownout** |

Supplier sketches remain valid for desktop tests via `RevHubTelemetrySource`, but robot code should use `AmperFtc.builder(hardwareMap)`.
