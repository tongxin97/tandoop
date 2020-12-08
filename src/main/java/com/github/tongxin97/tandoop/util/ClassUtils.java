package com.github.tongxin97.tandoop.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtils {
    public static Map<String, String> WRAPPER_PRIMITIVE_MAPPER = new HashMap<String, String>(){{
        put(Boolean.class.getName(), boolean.class.getName());
        put(boolean.class.getName(), boolean.class.getName());
        put(Character.class.getName(), char.class.getName());
        put(char.class.getName(), char.class.getName());
        put(Byte.class.getName(), byte.class.getName());
        put(byte.class.getName(), byte.class.getName());
        put(Short.class.getName(), short.class.getName());
        put(short.class.getName(), short.class.getName());
        put(Integer.class.getName(), int.class.getName());
        put(int.class.getName(), int.class.getName());
        put(Long.class.getName(), long.class.getName());
        put(long.class.getName(), long.class.getName());
        put(Float.class.getName(), float.class.getName());
        put(float.class.getName(), float.class.getName());
        put(Double.class.getName(), double.class.getName());
        put(double.class.getName(), double.class.getName());
        put(Void.class.getName(), void.class.getName());
        put(void.class.getName(), void.class.getName());
    }};


    private static final Set<String> PRIMITIVE_OR_WRAPPER_TYPES = new HashSet(Arrays.asList(
        Boolean.class.getName(),
        Character.class.getName(),
        Byte.class.getName(),
        Short.class.getName(),
        Integer.class.getName(),
        Long.class.getName(),
        Float.class.getName(),
        Double.class.getName(),
        Void.class.getName(),
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

    private static final Set<String> BASIC_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class.getName(),
            Character.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Long.class.getName(),
            Float.class.getName(),
            Double.class.getName(),
            Number.class.getName(),
            String.class.getName(),
            Object.class.getName(),
            boolean.class.getName(),
            char.class.getName(),
            byte.class.getName(),
            short.class.getName(),
            int.class.getName(),
            long.class.getName(),
            float.class.getName(),
            double.class.getName()
    ));

    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList(
        Boolean.class.getName(),
        boolean.class.getName()
    ));

    public static boolean isPrimitiveType(String c) {
        return PRIMITIVE_TYPES.contains(c);
    }

    public static boolean isPrimitiveOrWrapper(String c) {
        return PRIMITIVE_OR_WRAPPER_TYPES.contains(c);
    }

    public static boolean isBasicType(String c) {
        return BASIC_TYPES.contains(c);
    }

    public static boolean isBooleanOrWrapper(String c) {
        return BOOLEAN_TYPES.contains(c);
    }

    /**
     * Parse compound types like int[] or Set<String> by removing the [] and split by <> and ,
     * @param cType
     * @return Set of simple types
     */
    public static Set<String> parseCompoundType(String cType) {
        cType = cType.replace("[", "").replace("]", "").replace(" ", "");
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
    public static void collectInheritanceInfo(String className, Map<String, Set<String>> inheritanceMap, ClassLoader classLoader) {
        Set<String> types = parseCompoundType(className);

        for (String type: types) {
            if (isPrimitiveType(type) || inheritanceMap.containsKey(type) || type.equals("")) {
                continue;
            }
            Class c = null;
            try {
                c = Class.forName(type, true, classLoader);
            } catch (ClassNotFoundException e1) {
                int lastDot = type.lastIndexOf('.');
                if (lastDot >= 0) {
                    String nestedClassName = type.substring(0, lastDot) + '$' + type.substring(lastDot + 1);
                    try {
                        c = Class.forName(nestedClassName, true, classLoader);
                    } catch (Exception e2) {
                        System.err.println("[Error] collectInheritanceInfo: " + e2.getMessage());
                    }
                } else {
                    System.err.println("[Error] collectInheritanceInfo: " + e1.getMessage());
                }
            } catch (Exception e2) {
                System.err.println("[Error] collectInheritanceInfo: " + e2.getMessage());
            }

            inheritanceMap.put(type, collectSuperClassAndInterfaces(c, inheritanceMap, classLoader));
        }
    }

    private static Set<String> collectSuperClassAndInterfaces(Class c, Map<String, Set<String>> inheritanceMap, ClassLoader classLoader) {
        if (c == null) {
            return new HashSet<String>();
        }
        String name = c.getCanonicalName();
        if (inheritanceMap.containsKey(name)) {
            return inheritanceMap.get(name);
        }
        Set<String> superClassAndInterfaces = new HashSet<>();
        superClassAndInterfaces.add(name);
        superClassAndInterfaces.addAll(collectSuperClassAndInterfaces(c.getSuperclass(), inheritanceMap, classLoader));
        for (Class i: c.getInterfaces()) {
            superClassAndInterfaces.addAll(collectSuperClassAndInterfaces(i, inheritanceMap, classLoader));
        }
        inheritanceMap.put(name, superClassAndInterfaces);
        return superClassAndInterfaces;
    }
}
