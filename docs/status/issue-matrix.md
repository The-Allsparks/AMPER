# Issue matrix (AMPER #1–#16)

Generated for the `0.1.0-rc.1` software pass. Desktop tests are **not** Control Hub validation. Do not close hardware-only issues from CI.

| Issue | Title | Software | Hardware | This PR | Close? |
|-------|-------|----------|----------|---------|--------|
| #1 | Research and source verification | Implemented and tested (docs + link checker). Access date 2026-08-17 | n/a | Docs retained and extended | Close after maintainer review of citations |
| #2 | Phase 0 measurement abstraction | Implemented and tested | Not hardware-validated | Core monitor, validity, timestamps | Split: keep hardware items on #6 |
| #3 | REV Hub telemetry adapter | Partially implemented before; **FTC SDK adapter added** | Not hardware-validated | `AmperFtc` / `FtcVoltageSource` / `FtcMotorTelemetry` | Keep open until Hub measurements exist, or split software vs hardware |
| #4 | Logging format and export | AdvantageScope CSV + canonical `/AMPER` keys + desktop WPILOG converter | On-robot retrieve not validated | Fixture, sidecar, CI WPILOG artifact | Software criteria expanded; keep open until a team opens a Hub file in AdvantageScope **or** split |
| #5 | Phase 1 passive telemetry | Implemented and tested | Not hardware-validated | Warnings, telemetry sink, examples | Same as #2: software yes, hardware on #6 |
| #6 | Controlled robot characterization | Procedure + OpMode ready | **Not run** | `AmperCharacterizationOpMode`, test card | **Do not close** |
| #7 | Phase 2 drivetrain ramp limiting | Foundations implemented, **disabled by default**, experimental | Not characterized | `SlewRateLimiter`, `LocalProtection` | Keep open |
| #8 | Phase 2 stall/jam detection | Suspicion tracker + tests; opt-in local protection | Not characterized | Still warning-first | Keep open |
| #9 | Phase 3 voltage-state machine | Software foundation only; no output intervention | None | `VoltageStateMachine` | Keep open; not production |
| #10 | PID and feedforward constraint handling | Documented anti-windup signal `ConstrainedCommand` | None | Docs + type | Keep open |
| #11 | Phase 4 request/grant API | Stub unchanged; pass-through grants | n/a | Unchanged | Keep open |
| #12 | Phase 4 priority coordinator | Stub pass-through | n/a | Unchanged | Keep open |
| #13 | Phase 5 battery model and shadow prediction | Shadow recorder only; no ML | None | `ShadowSagPredictor` | Keep open |
| #14 | Phase 6 predictive load shaping | Not implemented | n/a | Out of scope | Keep open |
| #15 | Phase 7 adaptive profiles | Not implemented | n/a | Out of scope | Keep open |
| #16 | SystemCore capability review | Boundary type only | Blocked | `SystemCoreAdapterBoundary` | **Keep blocked** until authoritative FIRST/REV docs |

## Close policy used

- Close only when every **non-hardware** acceptance criterion is satisfied and remaining hardware work lives in a named validation issue (#6 or a split).
- Never close #6 from desktop tests.
- Never represent Phases 3–7 as ready.

Maintainers should comment on each GitHub issue with the matching row after merging this PR. This file is the source of truth when README, code, and issues disagree.
