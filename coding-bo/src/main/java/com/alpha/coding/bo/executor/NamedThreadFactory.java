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

    private static final String DEFAULT_POOL_NAME = "NamedPool";
    private static final String DEFAULT_THREAD_INDEX_PREFIX = "td";
    private static final Map<String, AtomicInteger> NAMED_POOL_MAP = new HashMap<>();
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;
    private final boolean daemon;

    public NamedThreadFactory() {
        this(null, null, false);
    }

    public NamedThreadFactory(String poolNamePrefix) {
        this(poolNamePrefix, null, false);
    }

    public NamedThreadFactory(String poolNamePrefix, boolean daemon) {
        this(poolNamePrefix, null, daemon);
    }

    public NamedThreadFactory(String poolNamePrefix, String threadIndexPrefix, boolean daemon) {
        String usePoolNamePrefix = poolNamePrefix == null ? DEFAULT_POOL_NAME : poolNamePrefix;
        String useThreadIndexPrefix = threadIndexPrefix == null ? DEFAULT_THREAD_INDEX_PREFIX : threadIndexPrefix;
        this.namePrefix = usePoolNamePrefix + "-" + getByPoolName(usePoolNamePrefix).getAndIncrement()
                + "-" + useThreadIndexPrefix + "-";
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        thread.setDaemon(daemon && !thread.isDaemon());
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }

    private static AtomicInteger getByPoolName(String poolName) {
        AtomicInteger atomicInteger = NAMED_POOL_MAP.get(poolName);
        if (atomicInteger == null) {
            synchronized(NamedThreadFactory.class) {
                atomicInteger = NAMED_POOL_MAP.get(poolName);
                if (atomicInteger == null) {
                    atomicInteger = new AtomicInteger(1);
                    NAMED_POOL_MAP.put(poolName, atomicInteger);
                }
            }
        }
        return atomicInteger;
    }

}
