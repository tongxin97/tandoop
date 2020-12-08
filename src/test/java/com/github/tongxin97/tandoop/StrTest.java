package com.github.tongxin97.tandoop;

import com.github.tongxin97.tandoop.util.Str;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class StrTest {
    @Test
    public void testSanitizeTypeString() {
        org.junit.Assert.assertEquals("java.util.Set60java.lang.String62", Str.sanitizeTypeString("java.util.Set<java.lang.String>"));
        org.junit.Assert.assertEquals("org.joda.time.DateTimeField9193", Str.sanitizeTypeString("org.joda.time.DateTimeField[]"));
    }

    @Test
    public void testParseNestedTypes() {
        String type = "org.apache.commons.collections4.IterableMap<K, V>";
        Set<String> generics = new HashSet<>();
        Str.parseNestedTypes(type, generics);
        org.junit.Assert.assertTrue(generics.toString().equals("[K, V]") || generics.toString().equals("[V, K]"));

        type = "java.lang.Iterable<E>";
        generics = new HashSet<>();
        Str.parseNestedTypes(type, generics);
        org.junit.Assert.assertEquals(generics.toString(), "[E]");
    }
}