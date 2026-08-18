# Risks and unresolved questions

| ID | Risk / question | Status |
|----|-----------------|--------|
| R1 | Exact Hub voltage where DS/control becomes unreliable on Allsparks robots | Open — needs characterization |
| R2 | `getCurrent` cost per motor per loop on season SDK | Open — measure in Phase 0 |
| R3 | Whether Hub battery current API is accurate enough for \(R_{eff}\) estimates | Open |
| R4 | Multi-hub: which voltage is authoritative under asymmetric wiring | Open |
| R5 | SystemCore electrical telemetry / brownout behavior | Blocked on authoritative docs |
| R6 | Counterbalanced elevator safe minimum effort vs mechanical failure modes | Open — mechanical + software joint design |
| R7 | Naive voltage compensation increasing sag near limits | Hypothesis — treat as dangerous until tested |
| R8 | Publishing as Maven artifact vs TeamCode copy | Deferred — remain standalone git library for now |
| R9 | Regenerative voltage spikes confusing sag detectors | Open |
| R10 | Autonomous trajectory timing vs slew limits | Open before Phase 2 in auto |

## Convention decisions already made

| Topic | Decision | Reason |
|-------|----------|--------|
| License | MIT | Match ViDAR |
| Visibility | Public | Match ViDAR / ftc-dev-tools OSS |
| Default branch | `main` | Org convention |
| Java | 8 source/target; CI Temurin 17 | Match FTC SDK 11.2 `build.common.gradle`, not ViDAR Java 11 |
| Package | `org.allsparks.amper` | Reusable library naming (vs `teamcode.vidar` copy style) |
| Intervention | Disabled by default | Safety |
