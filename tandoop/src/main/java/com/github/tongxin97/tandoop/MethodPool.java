package com.github.tongxin97.tandoop;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;

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
        int i = Utils.getRandomInt(MethodInfoList.size());
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
