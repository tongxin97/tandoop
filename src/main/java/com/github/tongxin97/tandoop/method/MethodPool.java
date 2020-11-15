package com.github.tongxin97.tandoop.method;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;

import com.github.tongxin97.tandoop.util.Rand;

/**
 * The class that stores the whole pool of methods.
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class MethodPool {
    public List<MethodInfo> MethodInfoList;

    public MethodPool() {
        MethodInfoList = new ArrayList<>();
    }

    public MethodInfo getRandomMethod() throws IllegalArgumentException {
        if (MethodInfoList.isEmpty()) {
            throw new IllegalArgumentException("MethodPool is empty.");
        }
        int i = Rand.getRandomInt(MethodInfoList.size());
        return MethodInfoList.get(i);
    }

    @Override
    public String toString() {
        String out = "MethodPool:";
        for (MethodInfo info: this.MethodInfoList) {
            out += "\n=====\n" + info;
        }
        return out;
    }
}
