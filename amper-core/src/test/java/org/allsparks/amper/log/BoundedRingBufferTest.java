package org.allsparks.amper.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoundedRingBufferTest {
    @Test
    void overflowDropsOldestAndKeepsInsertionOrder() {
        BoundedRingBuffer<Integer> ring = new BoundedRingBuffer<Integer>(3);
        ring.add(1);
        ring.add(2);
        ring.add(3);
        ring.add(4);
        ring.add(5);
        List<Integer> order = new ArrayList<Integer>();
        for (Integer value : ring) {
            order.add(value);
        }
        assertEquals(3, ring.size());
        assertEquals(2L, ring.droppedCount());
        assertEquals(Integer.valueOf(3), order.get(0));
        assertEquals(Integer.valueOf(4), order.get(1));
        assertEquals(Integer.valueOf(5), order.get(2));
        assertEquals(order, ring.snapshot());
    }
}
