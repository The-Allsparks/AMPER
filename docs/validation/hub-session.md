# Control Hub session (#6)

Adult supervision. Wheels off or restrained. **Do not induce a brownout.** Desktop `check` is not this session.

Goal: fill [results-template.md](results-template.md) and update [STATUS.md](STATUS.md). Those numbers also close the remaining [#41](https://github.com/The-Allsparks/AMPER/issues/41) cost rows (disabled vs passive loop cost).

## Before you start (~10 min)

1. Install AMPER into the season FTC SDK project per [install.md](../install.md). Use `main` or a tag **newer than** `v0.1.0-rc.1` (quality and Gradle 9.7 landed after that tag).
2. Copy `AmperCharacterizationOpMode` from `amper-examples` into TeamCode. Remove `@Disabled`.
3. Match motor names (`frontLeft`, `frontRight`, `backLeft`, `backRight`) to the hardware map, or change the OpMode.
4. Print this card and [hardware-test-card.md](hardware-test-card.md).
5. Label two packs: healthy vs weaker. Stop before over-discharge.

## Runs (change `mode` in code between OpMode restarts)

| Order | `Mode` | Duration | Record |
|-------|--------|----------|--------|
| 1 | `DISABLED` | 30 s, wheels off, normal rest loop | Mean/max **OpMode** loop µs (DS or logger). AMPER update µs should stay near-zero / n/a. This is **disabled-mode cost** for #41. |
| 2 | `VOLTAGE_ONLY` | Same motions | Mean/max OpMode loop µs **and** `meanUs` / `maxUs` from AMPER telemetry. This is **passive voltage-only cost** for #41. |
| 3 | `VOLTAGE_PLUS_ONE_CURRENT` | Light one-motor motion | Current-poll overhead vs step 2 |
| 4 | `VOLTAGE_PLUS_N_EVERY_LOOP` | Four drive motors | Overhead; note if the loop feels worse |
| 5 | `VOLTAGE_PLUS_N_ROUND_ROBIN` | Same as 4 | Overhead; pick a cadence that stays acceptable |

Then finish hardware-test-card steps 6–11 (missing sensor, healthy pack, weaker pack, multi-hub if present, motor-behavior unchanged, practice log export).

## After the session

1. Copy [results-template.md](results-template.md) to `docs/validation/results/YYYY-MM-DD-robot.md` with **measured** cells only.
2. Change [STATUS.md](STATUS.md) from “not yet run” to a dated summary. Do not invent loop times.
3. Comment on [#6](https://github.com/The-Allsparks/AMPER/issues/6) with the result file path. #41 can consume those numbers; do not enable Phase 2 from this session alone.

## Out of scope

- Phase 2+ actuation, gravity-hold (#27), combined-stack FORGE#4 (needs the rest of the Allsparks libraries on the same robot).
