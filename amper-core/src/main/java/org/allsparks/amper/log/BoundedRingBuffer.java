package org.allsparks.amper.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Bounded FIFO. Overflow drops the oldest element in O(1) instead of
 * {@code List.remove(0)}.
 */
final class BoundedRingBuffer<T> implements Iterable<T> {
    private final int capacity;
    private final ArrayDeque<T> items;
    private long dropped;

    BoundedRingBuffer(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayDeque<T>(capacity);
    }

    void add(T item) {
        if (items.size() >= capacity) {
            items.removeFirst();
            dropped++;
        }
        items.addLast(item);
    }

    int size() {
        return items.size();
    }

    long droppedCount() {
        return dropped;
    }

    void clear() {
        items.clear();
        dropped = 0L;
    }

    List<T> snapshot() {
        return Collections.unmodifiableList(new ArrayList<T>(items));
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
}
