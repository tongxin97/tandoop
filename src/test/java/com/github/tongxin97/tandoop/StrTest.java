package com.github.tongxin97.tandoop;

import com.github.tongxin97.tandoop.util.Str;
import org.junit.Test;

public class StrTest {
    @Test
    public void testSanitizeTypeString() {
        org.junit.Assert.assertEquals("java.util.Set60java.lang.String62", Str.sanitizeTypeString("java.util.Set<java.lang.String>"));
        org.junit.Assert.assertEquals("org.joda.time.DateTimeField9193", Str.sanitizeTypeString("org.joda.time.DateTimeField[]"));
    }
}
