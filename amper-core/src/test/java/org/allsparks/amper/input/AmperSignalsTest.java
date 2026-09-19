package org.allsparks.amper.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AmperSignalsTest {
    @Test
    void controlHubKeyIsStable() {
        assertEquals("amper/controlHubVoltage", AmperSignals.CONTROL_HUB_VOLTAGE.qualifiedName());
    }

    @Test
    void namedHubKeysStayDistinct() {
        assertEquals(
                "amper/hubVoltage.Expansion Hub 1",
                AmperSignals.hubVoltage("Expansion Hub 1").qualifiedName());
        assertNotEquals(AmperSignals.CONTROL_HUB_VOLTAGE, AmperSignals.hubVoltage("Control Hub"));
    }

    @Test
    void blankHubNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> AmperSignals.hubVoltage("  "));
    }
}
