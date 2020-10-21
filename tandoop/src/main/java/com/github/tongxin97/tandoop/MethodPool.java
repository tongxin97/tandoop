package com.github.tongxin97.tandoop;

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

    public MethodInfo getRandomMethod() throws Exception {
        if (MethodInfoList.isEmpty()) {
            throw new Exception("Method pool is empty.");
        }
        Integer i = rand.nextInt(MethodInfoList.size() - 1);
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
