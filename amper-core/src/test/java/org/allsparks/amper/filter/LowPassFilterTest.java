package org.allsparks.amper.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LowPassFilterTest {
    @Test
    void firstSampleInitializes() {
        LowPassFilter filter = new LowPassFilter(0.5);
        assertEquals(12.0, filter.update(12.0), 1e-9);
        assertTrue(filter.isInitialized());
    }

    @Test
    void smoothsSubsequentSamples() {
        LowPassFilter filter = new LowPassFilter(0.5);
        filter.update(10.0);
        assertEquals(11.0, filter.update(12.0), 1e-9);
    }

    @Test
    void nanDoesNotInitialize() {
        LowPassFilter filter = new LowPassFilter(0.5);
        assertTrue(Double.isNaN(filter.update(Double.NaN)));
        assertFalse(filter.isInitialized());
    }
}
