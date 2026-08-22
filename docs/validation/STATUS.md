# Hardware validation status

**Status: not yet run.**

No Control Hub measurements are in this repository. Desktop unit tests and simulated traces are **not** hardware validation.

Required physical results before calling Phase 0/1 competition-validated are listed in [hardware-test-card.md](hardware-test-card.md). Record numbers in [results-template.md](results-template.md) and store completed sheets here.

Issue tracking for this work is **[#6](https://github.com/The-Allsparks/AMPER/issues/6)**. Software for Phase 0/1 (#1–#5) is closed; remaining Hub evidence lives here. Epic [#41](https://github.com/The-Allsparks/AMPER/issues/41) also needs disabled-mode and passive-mode loop-cost numbers from the same session — see [ftc-integration-checklist.md](../status/ftc-integration-checklist.md).

Do not invent universal loop-time or current-poll numbers. Thresholds in `PowerPolicy` remain `CONSERVATIVE_PLACEHOLDER` until a labeled robot dataset exists.
