package org.allsparks.amper.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoopOverheadStatsTest {

    @Test
    void percentilesAreMonotonicOverTheRollingWindow() {
        LoopOverheadStats stats = new LoopOverheadStats();
        for (int i = 1; i <= 100; i++) {
            stats.offer(i * 1_000L);
        }
        long p50 = stats.percentileNanos(0.50);
        long p95 = stats.percentileNanos(0.95);
        long p99 = stats.percentileNanos(0.99);
        assertTrue(p50 > 0L);
        assertTrue(p95 >= p50);
        assertTrue(p99 >= p95);
        assertTrue(stats.maxNanos() >= p99);
        assertEquals(100L, stats.count());
    }

    @Test
    void emptyStatsDoNotThrow() {
        LoopOverheadStats stats = new LoopOverheadStats();
        assertEquals(0L, stats.percentileNanos(0.95));
        assertEquals(0L, stats.maxNanos());
        assertTrue(Double.isNaN(stats.meanNanos()));
    }
}
