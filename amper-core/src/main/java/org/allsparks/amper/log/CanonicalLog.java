package org.allsparks.amper.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bounded hardware-independent log. Sinks consume this model; they must not
 * feed back into measurement or policy.
 */
public final class CanonicalLog {
    private final int capacity;
    private final List<CanonicalSample> samples;
    private final Map<String, LogFieldSpec> schema = new LinkedHashMap<String, LogFieldSpec>();
    private final Map<String, LogValue> acceptedScratch = new LinkedHashMap<String, LogValue>();
    private final LogNameSanitizer names;
    private long dropped;
    private long typeMismatches;
    private long backwardTimestamps;
    private long originNanos = Long.MIN_VALUE;
    private long lastTimestampNanos = Long.MIN_VALUE;

    public CanonicalLog(int capacity) {
        this(capacity, new LogNameSanitizer());
    }

    public CanonicalLog(int capacity, LogNameSanitizer names) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.samples = new ArrayList<CanonicalSample>(capacity);
        this.names = names == null ? new LogNameSanitizer() : names;
    }

    public void register(LogFieldSpec spec) {
        Objects.requireNonNull(spec, "spec");
        LogFieldSpec existing = schema.get(spec.key());
        if (existing == null) {
            schema.put(spec.key(), spec);
            return;
        }
        if (existing.type() != spec.type()) {
            typeMismatches++;
        }
    }

    public void append(CanonicalSample sample) {
        Objects.requireNonNull(sample, "sample");
        long timestamp = sample.timestampNanos();
        if (originNanos == Long.MIN_VALUE) {
            originNanos = timestamp;
        }
        if (lastTimestampNanos != Long.MIN_VALUE && timestamp < lastTimestampNanos) {
            backwardTimestamps++;
            timestamp = lastTimestampNanos;
            sample = new CanonicalSample(timestamp, sample.values());
        }
        lastTimestampNanos = timestamp;

        Map<String, LogValue> accepted = acceptedScratch;
        accepted.clear();
        for (Map.Entry<String, LogValue> entry : sample.values().entrySet()) {
            LogValue value = entry.getValue();
            if (value == null) {
                continue;
            }
            LogFieldSpec spec = schema.get(entry.getKey());
            if (spec != null && spec.type() != value.type()) {
                typeMismatches++;
                continue;
            }
            if (value.present()) {
                accepted.put(entry.getKey(), value);
            }
        }
        if (samples.size() >= capacity) {
            samples.remove(0);
            dropped++;
        }
        samples.add(new CanonicalSample(timestamp, accepted));
    }

    public List<CanonicalSample> samples() {
        return Collections.unmodifiableList(samples);
    }

    public Map<String, LogFieldSpec> schema() {
        return Collections.unmodifiableMap(schema);
    }

    public LogNameSanitizer names() {
        return names;
    }

    public long droppedCount() {
        return dropped;
    }

    public long typeMismatchCount() {
        return typeMismatches;
    }

    public long backwardTimestampCount() {
        return backwardTimestamps;
    }

    public long originNanos() {
        return originNanos == Long.MIN_VALUE ? 0L : originNanos;
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return samples.size();
    }

    public void clear() {
        samples.clear();
        dropped = 0;
        typeMismatches = 0;
        backwardTimestamps = 0;
        originNanos = Long.MIN_VALUE;
        lastTimestampNanos = Long.MIN_VALUE;
        schema.clear();
    }
}
