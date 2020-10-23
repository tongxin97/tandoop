package com.github.tongxin97.tandoop;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The class that stores the whole pool of value
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

// TODO: use a common abstract class with MethodPool for getRandom
public class TypedValuePool<T> {
    private String type;
    private T nullValue;
    private List<T> values;
    private Random rand;

    public TypedValuePool(String type, List<T> vals) {
        rand = new Random();
        this.type = type;
        this.nullValue = null;
        if (vals != null) {
            this.values = new ArrayList(vals);
        }
    }

    public T getRandomValue() {
        if (values.isEmpty()) {
            if (this.type.equals("null")) {
                return null;
            }
            // ValuePool can't be empty
        }
        Integer i = rand.nextInt(values.size());
        return values.get(i);
    }

    public void addValue(T value) {
        values.add(value);
    }

    public String toString() {
        if (this.type.equals("null")) {
            return "null\n";
        }
        StringBuilder sb = new StringBuilder();
        for (T v: this.values) {
            sb.append(v);
            sb.append(",");
        }
        sb.append("\n");
        return sb.toString();
    }
}
