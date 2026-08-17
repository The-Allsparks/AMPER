package org.allsparks.amper.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AmperAnalyzeTest {
    @Test
    void reportsVoltageAndOverhead() {
        String csv = "# amper_csv_schema=1\n"
                + "timestampNanos,type,message,fields\n"
                + "0,LOOP_SAMPLE,observation,rawV=12.5000;filtV=12.5000;sensingValid=true;sumAbsCmd=0.2000;loopNs=12000;m0A=1.2500;m0Validity=VALID\n"
                + "20000000,LOOP_SAMPLE,observation,rawV=12.4000;filtV=12.4500;sensingValid=true;sumAbsCmd=0.8000;loopNs=18000;m0A=2.0000;m0Validity=VALID\n"
                + "20000000,STATE_TRANSITION,mechanism_start,motor=intake\n";
        String report = AmperAnalyze.analyze(csv);
        assertTrue(report.contains("Voltage vs time"));
        assertTrue(report.contains("12.5000"));
        assertTrue(report.contains("Loop overhead"));
        assertTrue(report.contains("mechanism_start"));
        assertTrue(report.contains("m0_A"));
        assertTrue(report.contains("not Control Hub validation"));
    }
}
