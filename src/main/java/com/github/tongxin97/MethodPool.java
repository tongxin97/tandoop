package com.github.tongxin97;

import com.github.tandoop.Method;

import java.util.List;
import java.util.Random;

/**
 * The class that stores the whole pool of methods.
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class MethodPool {
    private List<Method> methods;
    private Random rand;

    public MethodPool() {
        rand = new Random();
    }

    public Method getRandomMethod() throws Exception {
        if (methods.isEmpty()) {
            throw new Exception("Method pool is empty.");
        }
        Integer i = rand.nextInt(methods.size() - 1);
        return methods.get(i);
    }

    public void addMethods(String methodName) {
        // TODO: @tongxin97 create one Method add add it to list
    }

}
