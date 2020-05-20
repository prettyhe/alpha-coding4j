package com.alpha.coding.bo.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NamedThreadFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final String defaultPoolName = "NamedPool";
    private static final Map<String, AtomicInteger> namedPoolMap = new HashMap();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;

    public NamedThreadFactory() {
        this(null, false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        String usePrefix = prefix == null ? defaultPoolName : prefix;
        namePrefix = usePrefix + "-" + getByPoolName(usePrefix).getAndIncrement() + "-td-";
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        t.setDaemon((daemon && !t.isDaemon()) ? true : false);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }

    private static AtomicInteger getByPoolName(String poolName) {
        AtomicInteger atomicInteger = namedPoolMap.get(poolName);
        if (atomicInteger == null) {
            synchronized(NamedThreadFactory.class) {
                atomicInteger = namedPoolMap.get(poolName);
                if (atomicInteger == null) {
                    atomicInteger = new AtomicInteger(1);
                    namedPoolMap.put(poolName, atomicInteger);
                }
            }
        }
        return atomicInteger;
    }

}
