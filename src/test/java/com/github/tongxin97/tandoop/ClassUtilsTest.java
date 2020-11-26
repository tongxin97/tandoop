package com.github.tongxin97.tandoop;

import com.github.tongxin97.tandoop.util.ClassUtils;
import com.github.tongxin97.tandoop.value.ValueInfo;
import com.github.tongxin97.tandoop.value.VarInfo;
import org.junit.Test;

import java.util.*;

public class ClassUtilsTest {
    @Test
    public void testParseCompoundType() {
        Set<String> types = ClassUtils.parseCompoundType("java.lang.String[]");
        org.junit.Assert.assertEquals(types.iterator().next(), "java.lang.String");
        types = ClassUtils.parseCompoundType("java.util.Set<org.joda.time.DateTimeFieldType>");
        org.junit.Assert.assertTrue(
                types.toString().equals("[java.util.Set, org.joda.time.DateTimeFieldType]") ||
                        types.toString().equals("[org.joda.time.DateTimeFieldType, java.util.Set]")
        );

//        HashSet<ArrayList<VarInfo>> v = new HashSet<>();
//        Set<ArrayList<VarInfo>> s = (Set<ArrayList<VarInfo>>) v;
//        VarInfo[] v = new VarInfo[3];
//        ValueInfo[] vi = (ValueInfo[]) v;
    }

    @Test
    public void testGetSubCollectionsTypes() {
        Map<String, Set<String>> inheritance = new HashMap<>();

        inheritance.put("java.util.Collections", new HashSet<>(Arrays.asList("java.util.List", "java.util.Set")));
        Set<String> subTypes = ClassUtils.getSubCollectionsTypes("java.util.Collections<int>", inheritance);
        org.junit.Assert.assertNotNull(subTypes);
        org.junit.Assert.assertTrue(subTypes.contains("java.util.List<int>"));
        org.junit.Assert.assertTrue(subTypes.contains("java.util.Set<int>"));

        inheritance.put("int", new HashSet<>(Arrays.asList("int")));
        subTypes = ClassUtils.getSubCollectionsTypes("int[]", inheritance);
        org.junit.Assert.assertNull(subTypes);
    }
}
