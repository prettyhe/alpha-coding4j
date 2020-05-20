package com.alpha.coding.common.assist.load;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MemoryClassLoader
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MemoryClassLoader extends URLClassLoader {

    private static Map<String, byte[]> CLASS_BYTES_CACHE = new ConcurrentHashMap<>(64);
    private static MemoryClassLoader instance = new MemoryClassLoader();

    private MemoryClassLoader() {
        super(new URL[0], MemoryClassLoader.class.getClassLoader());
    }

    public static MemoryClassLoader getInstance() {
        return instance;
    }

    public void loadClassBytes(Map<String, byte[]> classBytes) {
        classBytes.putAll(classBytes);
    }

    public MemoryClassLoader fluentLoadClassBytes(Map<String, byte[]> classBytes) {
        classBytes.putAll(classBytes);
        return this;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = CLASS_BYTES_CACHE.get(name);
        if (buf == null) {
            return super.findClass(name);
        }
        CLASS_BYTES_CACHE.remove(name);
        return defineClass(name, buf, 0, buf.length);
    }

    public Class<?> loadAndResolveClass(String name) throws ClassNotFoundException {
        return super.loadClass(name, true);
    }
}
