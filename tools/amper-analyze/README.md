# AMPER analyze

Desktop-only tools. Do **not** add these to a TeamCode/on-robot dependency.

## Java

From the repository root:

```bash
./gradlew :amper-tools:run --args="path/to/amper-session.csv"
```

Windows:

```powershell
.\gradlew.bat :amper-tools:run --args="path\to\amper-session.csv"
```

The report includes voltage vs time (first rows), filtered voltage, command/activity markers, current traces when present, and loop-overhead summary. It is **not** Control Hub validation.

## Python

```bash
python tools/amper-analyze/amper_analyze.py path/to/amper-session.csv --plot voltage.png
```

`--plot` requires matplotlib. The script still prints a text summary without it.
