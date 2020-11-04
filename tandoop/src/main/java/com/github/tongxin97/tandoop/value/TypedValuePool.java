package com.github.tongxin97.tandoop.value;

import java.lang.StringBuilder;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import com.github.tongxin97.tandoop.util.Rand;

/**
 * The class that stores the whole pool of value for certain type.
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class TypedValuePool<T> {
    private String type;
    private T nullValue;
    private Set<T> values;

    public TypedValuePool(String type, List<T> vals) {
        this.type = type;
        this.nullValue = null;
        if (vals != null) {
            this.values = new HashSet(vals);
        }
    }

    public T getRandomValue() {
        if (values.isEmpty()) {
            if (this.type.equals("null")) {
                return null;
            }
            // ValuePool can't be empty
        }
        int idx = Rand.getRandomInt(values.size());
        int i = 0;
        for (T v: values) {
            if (i == idx) {
                return v;
            }
            i++;
        }
        return null; // won't reach here
    }

    public void addValue(T value) {
        if (value == null) {
            return;
        }
        values.add(value);
    }

    public boolean contains(T value) {
        if (value == null) {
            return false;
        }
        return this.values.contains(value);
    }

    public String toString() {
        if (this.type != null && this.type.equals("null")) {
            return "null\n";
        }
        StringBuilder b = new StringBuilder();
        for (T v: this.values) {
            b.append(String.format("%s,", v));
        }
        b.append("\n");
        return b.toString();
    }
}
