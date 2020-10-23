package com.github.tongxin97.tandoop;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The class that stores the whole pool of methods.
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class MethodPool {
    public List<MethodInfo> MethodInfoList;
    private Random rand;

    public MethodPool() {
        rand = new Random();
        MethodInfoList = new ArrayList<>();
    }

    public MethodInfo getRandomMethod() throws IllegalArgumentException {
        if (MethodInfoList.isEmpty()) {
            throw new IllegalArgumentException("MethodPool is empty.");
        }
        int i = rand.nextInt(MethodInfoList.size());
        return MethodInfoList.get(i);
    }

    @Override
    public String toString() {
        String out = "";
        for (MethodInfo info: this.MethodInfoList) {
            out += "=====\n" + info;
        }
        return out;
    }
}
