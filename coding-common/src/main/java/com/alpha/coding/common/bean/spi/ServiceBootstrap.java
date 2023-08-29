package com.alpha.coding.common.bean.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceBootstrap {

    public static <S> S loadFirst(Class<S> clazz) {
        Iterator<S> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has"
                            + " the right implementation class!",
                    clazz.getName()));
        }
        return iterator.next();
    }

    public static <S> Iterator<S> loadAll(Class<S> clazz) {
        ServiceLoader<S> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }

    public static <S extends Ordered> List<S> loadAllOrdered(Class<S> clazz) {
        Iterator<S> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has"
                            + " the right implementation class!",
                    clazz.getName()));
        }
        List<S> candidates = Lists.newArrayList(iterator);
        // the smaller order has higher priority
        candidates.sort(Comparator.comparingInt(Ordered::getOrder));

        return candidates;
    }

    public static <S extends Ordered> S loadPrimary(Class<S> clazz) {
        List<S> candidates = loadAllOrdered(clazz);
        return candidates.get(0);
    }

    private static ClassLoader findClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载目录下所有的service
     * <li>内容格式为k=v形式</li>
     */
    public static <S> Multimap<String, S> loadAll(String dir, Class<S> type) {
        String fileName = (dir.endsWith("/") ? dir : (dir + "/")) + type.getName();
        final Multimap<String, S> multimap = ArrayListMultimap.create();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls == null) {
                throw new IllegalStateException("No file defined for " + fileName);
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                            StandardCharsets.UTF_8))) {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            final int ci = line.indexOf('#');
                            if (ci >= 0) {
                                line = line.substring(0, ci);
                            }
                            line = line.trim();
                            if (line.length() > 0) {
                                try {
                                    String name = null;
                                    int i = line.indexOf('=');
                                    if (i <= 0) {
                                        log.warn("service format error for {} in {}, must like k=v", line, fileName);
                                        continue;
                                    }
                                    name = line.substring(0, i).trim();
                                    line = line.substring(i + 1).trim();
                                    if (line.length() <= 0) {
                                        log.warn("Illegal service for {} in {}", name, fileName);
                                        continue;
                                    }
                                    final S s = load(line, type, classLoader);
                                    multimap.put(name, s);
                                } catch (Throwable t) {
                                    if (t.getCause() instanceof ClassNotFoundException
                                            || t.getCause() instanceof LinkageError) {
                                        log.warn("Failed to load class(interface: " + type + ", class line: "
                                                + line + ") in " + url + ", cause: " + t.getCause().getClass().getName()
                                                + ", msg: " + t.getMessage());
                                    } else {
                                        log.warn("Failed to load class(interface: " + type + ", class line: "
                                                + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    log.error("Exception when load extension class(interface: " +
                            type + ", class file: " + url + ") in " + url, t);
                }
            }
        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
        return multimap;
    }

    /**
     * 加载目录下所有的service，按顺序排序
     * <li>内容格式为k=v形式</li>
     */
    public static <S extends Ordered> Multimap<String, S> loadAllOrdered(String dir, Class<S> type) {
        final Multimap<String, S> multimap = loadAll(dir, type);
        multimap.keySet().forEach(k -> multimap.get(k).stream().sorted(Comparator.comparingInt(Ordered::getOrder)));
        return multimap;
    }

    public static <S> S load(String name, Class<S> target, ClassLoader cl) {
        Objects.requireNonNull(name, "Service interface cannot be null");
        ClassLoader loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        AccessControlContext acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        Supplier<S> supplier = () -> {
            Class<?> c = null;
            try {
                c = Class.forName(name, true, loader);
            } catch (ClassNotFoundException x) {
                fail(target, "Provider " + name + " occur ClassNotFoundException for " + x.getMessage(), x);
            } catch (LinkageError x) {
                fail(target, "Provider " + name + " occur " + x.getClass().getSimpleName()
                        + " for " + x.getMessage(), x);
            }
            if (!target.isAssignableFrom(c)) {
                fail(target, "Provider " + name + " not a subtype");
            }
            try {
                return target.cast(c.newInstance());
            } catch (Throwable x) {
                fail(target, "Provider " + name + " could not be instantiated", x);
            }
            throw new Error();
        };
        if (acc == null) {
            return supplier.get();
        } else {
            PrivilegedAction<S> action = supplier::get;
            return AccessController.doPrivileged(action, acc);
        }
    }

    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, String msg, Throwable cause)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

}
