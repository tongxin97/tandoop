package com.github.tongxin97.tandoop.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Generate all subtypes for the given collection type, eg. Collections<String> -> {Set<String>, etc}
     * @param type given collection type
     * @param inheritanceMap map to find the sub types
     * @return subColTypes if exists, null otherwise.
     */
    public static Set<String> getSubCollectionsTypes(String type,  Map<String, Set<String>> inheritanceMap) {
        Set<String> subColTypes = new HashSet<>();
        if (!hasSubCollectionsTypes(type, inheritanceMap)) {
            return null;
        }
        String[] types = type.split("<|\\[", 2); // only split to 2 parts
        String collectionType = types[0];
        // generate subtypes if there are any: replace collectionType with its subtype in type
        subColTypes.addAll(inheritanceMap.get(collectionType).stream().map(
                t -> type.replace(collectionType, t)
        ).collect(Collectors.toSet()));
        return subColTypes;
    }

    /**
     *
     * @param type
     * @param inheritanceMap
     * @return true if sub-col-types exist for the given type, false otherwise.
     */
    private static boolean hasSubCollectionsTypes(String type, Map<String, Set<String>> inheritanceMap) {
        String[] types = type.split("<|\\[", 2); // only split to 2 parts
        return types.length == 2 && inheritanceMap.containsKey(types[0]) && inheritanceMap.get(types[0]).size() > 1;
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
            type = type.trim();
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
