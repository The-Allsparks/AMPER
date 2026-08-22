# Issue matrix (AMPER #1–#16)

Updated: 2026-08-22. Desktop tests are **not** Control Hub validation. Do not close hardware-only issues from CI.

Later findings live in the [initial deep audit](../audits/initial-deep-audit.md) and [priority ledger](priority-ledger.md). Do not duplicate #1–#16.

| Issue | Title | Software | Hardware | Disposition |
|-------|-------|----------|----------|-------------|
| #1 | Research and source verification | Docs + link checker on `main` | n/a | **Close** — living docs continue without an open tracker |
| #2 | Phase 0 measurement abstraction | Core monitor, validity, timestamps | Not Hub-validated | **Close software**; Hub cost/validity on [#6](https://github.com/The-Allsparks/AMPER/issues/6) |
| #3 | REV Hub telemetry adapter | `AmperFtc` / voltage + motor adapters; `sdk-compile` green | Not Hub-validated | **Close software**; Hub evidence on #6 |
| #4 | Logging format and export | AdvantageScope CSV, `/AMPER` keys, desktop WPILOG tools | On-robot retrieve not validated | **Close software**; Hub file retrieve/open on #6 |
| #5 | Phase 1 passive telemetry | Warnings, telemetry sink, examples | Not Hub-validated | **Close software**; Hub evidence on #6 |
| #6 | Controlled robot characterization | Procedure + OpMode ready | **Not run** | **Keep open** — sole Hub evidence issue for Phase 0/1 |
| #7 | Phase 2 drivetrain ramp limiting | Foundations, default-off | Not characterized | Keep open; blocked on #6 + flags |
| #8 | Phase 2 stall/jam detection | Suspicion tracker; opt-in local protection | Not characterized | Keep open; blocked on #6 |
| #9 | Phase 3 voltage-state machine | Foundation only; no output intervention | None | Keep open; not production |
| #10 | PID and feedforward constraints | `ConstrainedCommand` type + docs | None | Keep open |
| #11 | Phase 4 request/grant API | Stub pass-through | n/a | Keep open |
| #12 | Phase 4 priority coordinator | Stub pass-through | n/a | Keep open |
| #13 | Phase 5 shadow prediction | Shadow recorder only | None | Keep open |
| #14 | Phase 6 predictive load shaping | Not implemented | n/a | Keep open |
| #15 | Phase 7 adaptive profiles | Not implemented | n/a | Keep open |
| #16 | SystemCore capability review | Boundary type only | Blocked | **Keep blocked** until authoritative FIRST/REV docs |

## Close policy used

- Close only when every **non-hardware** acceptance criterion is satisfied and remaining hardware work lives in [#6](https://github.com/The-Allsparks/AMPER/issues/6).
- Never close #6 from desktop tests.
- Never represent Phases 3–7 as ready.

This file is the source of truth when README, code, and issues disagree.
