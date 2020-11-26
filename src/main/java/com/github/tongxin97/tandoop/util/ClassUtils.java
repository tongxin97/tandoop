package com.github.tongxin97.tandoop.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassUtils {
    private static final Set<String> PRIMITIVE_TYPES = new HashSet(Arrays.asList(
            boolean.class.getName(),
            char.class.getName(),
            byte.class.getName(),
            short.class.getName(),
            int.class.getName(),
            long.class.getName(),
            float.class.getName(),
            double.class.getName(),
            void.class.getName()
    ));

    public static boolean isPrimitiveType(String c) {
        return PRIMITIVE_TYPES.contains(c);
    }

    /**
     * Parse compound types like int[] or Set<String> by removing the [] and split by <> and ,
     * @param cType
     * @return Set of simple types
     */
    public static Set<String> parseCompoundType(String cType) {
        cType = cType.replace("[", "").replace("]", "");
        return new HashSet<>(Arrays.asList(cType.split("<|>|,")));
    }

    /**
     *  Add all superclasses' names in inheritanceMap (inverted mapping: super class -> set of sub classes)
     * @param className
     * @param inheritanceMap
     * @param classLoader
     */
    public static void collectSubClassInfo(String className, Map<String, Set<String>> inheritanceMap, ClassLoader classLoader) {
        Set<String> types = parseCompoundType(className);

        for (String type: types) {
            Set<String> tmpNames = new HashSet<>();
            try {
                Class c = Class.forName(type, true, classLoader);
                while (c != null) {
                    String canoName = c.getCanonicalName(); // the canonical name is the name that would be used in an import statement
                    tmpNames.add(canoName);
                    if (inheritanceMap.containsKey(canoName)) {
                        inheritanceMap.get(canoName).addAll(tmpNames);
                    } else {
                        inheritanceMap.put(canoName, new HashSet<>(tmpNames));
                    }
                    c = c.getSuperclass();
                }
            } catch (ClassNotFoundException e) {
                if (!isPrimitiveType(type)) {
                    System.err.printf("Class %s not found.\n", type);
                }
            } catch (Exception e) {
                System.err.println("Class.forName: " + e.getMessage());
            }
        }
    }
}
