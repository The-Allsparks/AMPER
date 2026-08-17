package org.allsparks.amper.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** One timestamped set of canonical field values. Internal time is integer nanoseconds. */
public final class CanonicalSample {
    private final long timestampNanos;
    private final Map<String, LogValue> values;

    public CanonicalSample(long timestampNanos, Map<String, LogValue> values) {
        this.timestampNanos = timestampNanos;
        this.values = Collections.unmodifiableMap(new LinkedHashMap<String, LogValue>(
                values == null ? Collections.<String, LogValue>emptyMap() : values));
    }

    public long timestampNanos() {
        return timestampNanos;
    }

    public Map<String, LogValue> values() {
        return values;
    }

    public LogValue get(String key) {
        return values.get(key);
    }

    public static Builder at(long timestampNanos) {
        return new Builder(timestampNanos);
    }

    public static final class Builder {
        private final long timestampNanos;
        private final Map<String, LogValue> values = new LinkedHashMap<String, LogValue>();

        private Builder(long timestampNanos) {
            this.timestampNanos = timestampNanos;
        }

        public Builder put(String key, LogValue value) {
            Objects.requireNonNull(key, "key");
            if (value != null && value.present()) {
                values.put(key, value);
            }
            return this;
        }

        public Builder putDouble(String key, double value) {
            return put(key, LogValue.ofDouble(value));
        }

        public Builder putBoolean(String key, boolean value) {
            return put(key, LogValue.ofBoolean(value));
        }

        public Builder putString(String key, String value) {
            return put(key, LogValue.ofString(value));
        }

        public Builder putInt64(String key, long value) {
            return put(key, LogValue.ofInt64(value));
        }

        public CanonicalSample build() {
            return new CanonicalSample(timestampNanos, values);
        }
    }
}
