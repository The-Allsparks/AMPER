# Sibling electrical contracts (AMPER provider view)

AMPER observes electrical state on an FTC robot. It **does not own actuators** and has **no compile-time dependency** on MIMIC, BEACON, HELM, or TRACE. The robot OpMode is the composition root.

Combined-stack acceptance is owned by [FORGE#4](https://github.com/The-Allsparks/FORGE/issues/4). This page is a **written contract only** — no sibling JARs are required for Phase 0/1.

Schema authority: existing `/AMPER` keys in [field-selection.md](../logging/field-selection.md) and [fixtures/amper-advantagescope-table.schema.json](../logging/fixtures/amper-advantagescope-table.schema.json). Additive optional fields only; do not rename published keys in this document.

## Contract table

| Consumer | What AMPER provides today | Transport / API | Version | When absent |
| -------- | ------------------------- | --------------- | ------- | ----------- |
| **MIMIC** | Observed bus voltage, per-motor command/current snapshots, Phase 0/1 identity grants (`RequestedEffort` = `GrantedEffort`, `Constrained` = false) | `/AMPER/System/*`, `/AMPER/Motors/*`, `/AMPER/Mechanisms/*` in AdvantageScope CSV; future narrow Java snapshot type TBD | schema `1`, library `0.1.0-rc.1` | MIMIC runs unchanged; no power envelope from AMPER |
| **BEACON** | Driver-facing `PowerState`, validity, stall suspicion flags, loop overhead counters | `/AMPER/System/PowerState`, `/AMPER/System/MeasurementValidity`, `/AMPER/Motors/*/StallSuspected`, `/AMPER/Performance/*`; DS keys `AMPER`, `AMPER.V`, `AMPER.valid` | schema `1` | BEACON omits electrical health; conventional teleop continues |
| **HELM** | Passive capability envelope: filtered voltage, min match voltage, selected-motors current sum, `PowerState` classification | Read-only snapshot from `AmperSession` + `/AMPER/System/*` exports; **no** chassis or mechanism authority | schema `1` | HELM stays `OFF` / observe-only per FORGE enablement |
| **TRACE** | Time-series rows under `/AMPER` with monotonic seconds timestamps; event rows `/AMPER/Events/Type` + `Message` | AdvantageScope table CSV + `.schema.json` sidecar; optional DS telemetry mirror | schema `1`, event names from `PowerEventType` | TRACE records other namespaces; AMPER CSV still written locally |

## Future request/grant (Phase 4+, not implemented)

| Direction | Types | Status |
| --------- | ----- | ------ |
| MIMIC → AMPER | `PowerRequest` (effort, priority, safety/gravity flags, estimated amps) | Type exists; **pass-through only** — `PowerCoordinator.allocate` returns unrestricted grants |
| AMPER → MIMIC | `PowerGrant` (`allowedEffort`, `delayed`, `PowerLimitReason`, `confidence`) | Type exists; Phase 0/1 **must not** use grants to change motor output |
| Subsystem → AMPER | `ConstrainedCommand` after optional Phase 2 `LocalProtection` | Experimental dual opt-in (`localProtectionAllowed` + per-subsystem enable); session flag is a kill switch when using `fromPolicy` / `AmperSession.localProtection`; still not auto-wired into observe; Hub evidence (#6) still required before competition enable |

Do not add compile-time edges between Allsparks libraries for these types. Adapter modules belong in TeamCode until Phase 2+ evidence exists.

## Shared conventions (FORGE-aligned)

| Topic | AMPER rule |
| ----- | ---------- |
| Timestamps | Internal nanoseconds; exported CSV uses seconds (monotonic within a session) |
| Sample age | `/AMPER/Hubs/*/SampleAgeSeconds`, motor current age fields — empty when unknown |
| Validity | `MeasurementValidity` explicit; never silent `0.0` volts or invented current |
| Units | Volts, amperes, command effort dimensionless ≈ `[-1, 1]`, velocity in encoder ticks/s |
| Telemetry namespaces | DS: `AMPER`, `AMPER.V`, …; logs: `/AMPER/...` only |
| Disable | `AmperPolicies.disabled()` — no hardware reads; conventional teleop unaffected |

## Ownership boundaries

- **AMPER owns:** voltage/current observation, validity classification, passive warnings, bounded logging, optional future grants.
- **AMPER does not own:** motor `setPower` / `setVelocity`, mechanism interlocks, DS link recovery, match strategy, Hub brownout behavior.
- **Robot application owns:** HardwareMap construction, lifecycle calls (`initialize` / `start` / `observe` / `stop`), when to enable Phase 2+.

## Compatibility

- Phase 0/1 consumers may rely on keys listed in the schema sidecar.
- New optional `/AMPER` fields require a schema version bump and a fixture update.
- Breaking renames require a new schema major version and a migration note — not planned for 0.1.x.

## Related

- [integration.md](../power-management/integration.md) — OpMode lifecycle
- [stack-acceptance (FORGE)](https://github.com/The-Allsparks/FORGE/blob/curriculum/season-foundation/docs/stack-acceptance.md)
- Issues: [#41](https://github.com/The-Allsparks/AMPER/issues/41), [#44](https://github.com/The-Allsparks/AMPER/issues/44)
