#!/usr/bin/env python3
"""Desktop AMPER CSV summary. Optional matplotlib graphs if installed.

This script is not an on-robot dependency and is not hardware validation.
"""
from __future__ import annotations

import argparse
import csv
import sys
from pathlib import Path
from typing import Dict, List, Tuple


def parse_fields(raw: str) -> Dict[str, str]:
    out: Dict[str, str] = {}
    if not raw:
        return out
    for part in raw.split(";"):
        if "=" not in part:
            continue
        key, value = part.split("=", 1)
        out[key] = value
    return out


def main() -> int:
    parser = argparse.ArgumentParser(description="Summarize an AMPER schema-1 CSV")
    parser.add_argument("csv_path")
    parser.add_argument("--plot", help="optional PNG output if matplotlib is installed")
    args = parser.parse_args()
    path = Path(args.csv_path)
    text = path.read_text(encoding="utf-8")
    lines = [line for line in text.splitlines() if line and not line.startswith("#")]
    if not lines:
        print("empty csv", file=sys.stderr)
        return 1
    reader = csv.reader(lines)
    header = next(reader)
    times = []
    raw_v = []
    filt_v = []
    cmds = []
    currents = []
    loop_ns = []
    markers = []
    for row in reader:
        if len(row) < 3:
            continue
        t = int(row[0])
        kind = row[1]
        message = row[2]
        fields = parse_fields(row[3] if len(row) > 3 else "")
        if kind in ("LOOP_SAMPLE", "SENSOR_INVALID"):
            times.append(t)
            raw_v.append(float(fields.get("rawV", "nan")))
            filt_v.append(float(fields.get("filtV", "nan")))
            cmds.append(float(fields.get("sumAbsCmd", "nan")))
            currents.append(float(fields.get("m0A", "nan")))
            loop_ns.append(int(fields.get("loopNs", "0")))
        elif kind in ("STATE_TRANSITION", "STALL_SUSPECTED", "VOLTAGE_WARNING"):
            markers.append((t, kind, message))
    print("AMPER desktop analysis (not Control Hub validation)")
    print(f"samples: {len(times)}")
    print(f"markers: {len(markers)}")
    if loop_ns:
        print(f"mean loop ns: {sum(loop_ns) / len(loop_ns):.1f}")
        print(f"max loop ns: {max(loop_ns)}")
    if raw_v:
        finite = [v for v in raw_v if v == v]
        if finite:
            print(f"raw V min/max: {min(finite):.4f} / {max(finite):.4f}")
    for marker in markers[:20]:
        print(f"marker t={marker[0]} {marker[1]} {marker[2]}")
    if args.plot:
        try:
            import matplotlib.pyplot as plt
        except ImportError:
            print("matplotlib not installed; skipping plot", file=sys.stderr)
            return 0
        fig, axes = plt.subplots(3, 1, sharex=True, figsize=(10, 8))
        axes[0].plot(times, raw_v, label="raw V")
        axes[0].plot(times, filt_v, label="filtered V")
        axes[0].legend()
        axes[0].set_ylabel("volts")
        axes[1].plot(times, cmds, label="sumAbsCmd")
        for t, kind, _message in markers:
            axes[1].axvline(t, color="tab:red", alpha=0.3)
        axes[1].set_ylabel("command")
        axes[2].plot(times, currents, label="m0 A")
        axes[2].set_ylabel("amps")
        axes[2].set_xlabel("t_ns")
        fig.tight_layout()
        fig.savefig(args.plot)
        print(f"wrote {args.plot}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
