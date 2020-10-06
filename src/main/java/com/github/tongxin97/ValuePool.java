package com.github.tongxin97;

import java.util.List;
import java.util.Random;

/**
 * The class that stores the whole pool of value
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class ValuePool<T> {
    private List<T> values;
    private Random rand;

    public ValuePool() {
        rand = new Random();
    }

    public T getRandomValue() throws Exception {
        if (values.isEmpty()) {
            throw new Exception("Value pool is empty.");
        }
        Integer i = rand.nextInt(values.size() - 1);
        return values.get(i);
    }

    public void addValue(T value) {
        values.add(value);
    }

}
