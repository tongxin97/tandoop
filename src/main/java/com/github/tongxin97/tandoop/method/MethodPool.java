package com.github.tongxin97.tandoop.method;

import java.lang.IllegalArgumentException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import com.github.tongxin97.tandoop.Tandoop;
import com.github.tongxin97.tandoop.util.Rand;

/**
 * The class that stores the whole pool of methods.
 *
 * @author Tangrizzly
 * on 2020/10/5
 */

public class MethodPool {
    public List<MethodInfo> MethodInfoList;
    public Map<String, Set<MethodInfo>> classToMethods;

    public MethodPool() {
        MethodInfoList = new ArrayList<>();
        classToMethods = new HashMap<>();
    }

    public void addMethod(MethodInfo m) {
        String fullClassName = m.getFullyQualifiedClassName();
        if (classToMethods.containsKey(fullClassName) && classToMethods.get(fullClassName).contains(m)) {
            return;
        }
        if (!classToMethods.containsKey(fullClassName)) {
            classToMethods.put(fullClassName, new HashSet<>());
        }
        classToMethods.get(fullClassName).add(m);
        MethodInfoList.add(m);
    }

    public void addParentMethodsToSubClasses() {
        Map<String, Set<MethodInfo>> copyOfClassToMethods = classToMethods.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(e.getValue()))
        );
        for (String superClass: Tandoop.inheritanceMap.keySet()) {
            for (String subClass: Tandoop.inheritanceMap.get(superClass)) {
                if (!copyOfClassToMethods.containsKey(superClass) || subClass.equals(superClass)) {
                    continue;
                }
                for (MethodInfo m: copyOfClassToMethods.get(superClass)) {
                    if (m.isConstructor) {
                        continue; // don't inherit constructors
                    }
                    int p = subClass.lastIndexOf(".");
                    String packageName = subClass.substring(0, p);
                    String className = subClass.substring(p+1);
                    MethodInfo newMethod = new MethodInfo(m, className, packageName);
//                    System.out.printf("Added %s to subclass %s of %s\n", m.Name, subClass, superClass);
                    addMethod(newMethod);
                }
            }
        }
    }

    public Set<MethodInfo> getConstructorsOfClass(String className) {
        if (!classToMethods.containsKey(className)) {
            return null;
        }
        return classToMethods.get(className).stream().filter(m -> m.isConstructor).collect(Collectors.toSet());
    }

    public MethodInfo getRandomMethod() throws IllegalArgumentException {
        if (MethodInfoList.isEmpty()) {
            throw new IllegalArgumentException("MethodPool is empty.");
        }
        return Rand.getRandomCollectionElement(MethodInfoList);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("MethodPool:\n");
        for (Map.Entry<String, Set<MethodInfo>> entry: classToMethods.entrySet()) {
            out.append(String.format("class %s: %s\n", entry.getKey(), entry.getValue()));
        }
        return out.toString();
    }
}
