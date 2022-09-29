package io.patriciadb.benchmarks.utils;

import java.util.Arrays;

public class KeyValue {
    final byte[] key;
    final byte[] value;

    public KeyValue(final byte[] key,final byte[] value) {
        this.key = key;
        this.value = value;
    }

    public byte[] key() {
        return key;
    }

    public byte[] value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValue keyValue = (KeyValue) o;
        return Arrays.equals(key, keyValue.key) && Arrays.equals(value, keyValue.value);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(key);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}
