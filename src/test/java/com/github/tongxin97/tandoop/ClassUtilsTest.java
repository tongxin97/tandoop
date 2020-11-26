package com.github.tongxin97.tandoop;

import com.github.tongxin97.tandoop.util.ClassUtils;
import org.junit.Test;

import java.util.Set;

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
    }
}
