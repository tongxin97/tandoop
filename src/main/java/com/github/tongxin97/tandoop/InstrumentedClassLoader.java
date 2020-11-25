package com.github.tongxin97.tandoop;

import java.util.Map;
import java.util.HashMap;

public class InstrumentedClassLoader extends ClassLoader {
    public InstrumentedClassLoader(ClassLoader parent) {
        super(parent);
    }

    public InstrumentedClassLoader(InstrumentedClassLoader parent) {
        super((ClassLoader) parent);
        this.definitions = parent.definitions;
    }

    public Map<String, byte[]> definitions = new HashMap<String, byte[]>();

    public void addDefinition(String name, byte[] bytes) {
        definitions.put(name, bytes);
    }
    
    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        final byte[] bytes = definitions.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.loadClass(name, resolve);
    }
}