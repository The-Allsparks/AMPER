# Integration contract

## Goals

- Phase 0/1 usable without rewriting every subsystem.
- Clear loop order for iterative OpModes, LinearOpModes, and command-based styles.

## Core types

See Java sources:

- `org.allsparks.amper.api.PowerRequest`
- `org.allsparks.amper.api.PowerGrant`
- `org.allsparks.amper.policy.PowerPolicy`
- `org.allsparks.amper.measure.PowerMonitor`

Effort fields use dimensionless \(\approx [-1, 1]\) unless your team documents otherwise in policy.

`estimatedCurrentAmps` may be `NaN` when unknown — do not invent currents.

## Iterative OpMode (Phase 0 sketch)

```java
// Pseudocode — wire suppliers to FTC SDK on the Control Hub.
PowerPolicy policy = PowerPolicy.defaults();
PowerTelemetrySource hub = RevHubTelemetrySource.voltageOnly(
    "Control Hub",
    () -> controlHubVoltageSensor.getVoltage());
PowerMonitor monitor = new PowerMonitor(
    new SystemNanoClock(),
    hub,
    motorTelemetryList,
    policy.voltageFilterAlpha(),
    policy.staleAfterNanos(),
    policy.minValidVolts(),
    policy.maxValidVolts());
PowerEventLogger logger = new PowerEventLogger(5000);

while (opModeIsActive()) {
    ElectricalObservation obs = monitor.update();
    logger.recordObservation(obs);
    // Existing subsystem code unchanged — still sets motor powers directly.
    telemetry.addData("V", obs.filteredVoltage().volts());
    telemetry.update();
}
```

## LinearOpMode

Same monitor update inside your `while (opModeIsActive())` sections. Prefer one observation update per control cycle, not once per motor write.

## Command-based / custom schedulers

1. Schedule a high-priority “AMPER observe” command/runnable each loop.
2. Subsystem commands compute requests.
3. Only if Phase 4+ enabled: allocate grants, then apply.

## Copying into TeamCode

Package today: `org.allsparks.amper.*` (library-style). Copy `src/main/java/org/allsparks/amper` into your FTC project or include this repo as a Gradle composite later. Align with your season SDK.

## Non-goals for initial integration

- Replacing Road Runner / Pedro / custom drivetrains
- Forcing every subsystem onto `PowerRequest` before Phase 4
- Enabling intervention flags in checked-in defaults
