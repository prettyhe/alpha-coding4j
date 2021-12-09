package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import com.alpha.coding.bo.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * SupplierHolder
 *
 * @version 1.0
 * Date 2020年04月13日
 */
@Slf4j
public class SupplierHolder<T> implements Supplier<T> {

    private static final String PROP_FILE_NAME;
    private static final String PROPERTY_KEY;
    private static volatile Boolean enableLogFromProperty;

    static {
        final String propFileName = "/META-INF/project-comm.properties";
        Properties properties = null;
        try {
            properties = CommonUtil.readFromClasspathFile(propFileName);
        } catch (Throwable e) {
            log.warn("load {} fail, {}", propFileName, e.getMessage());
        }
        if (properties == null) {
            properties = new Properties();
        }
        PROP_FILE_NAME = properties.getProperty("project.config.file.name", "system-common.properties");
        PROPERTY_KEY = properties.getProperty("property.key.supplier.holder.log.enable", "supplier.holder.log.enable");
    }

    private volatile T target;
    private final String tag;
    private final Supplier<T> supplier;
    private final Function<Object, String> logText;
    private Boolean enableLog;

    public static <T> SupplierHolder<T> of(Supplier<T> supplier) {
        return new SupplierHolder<>(supplier);
    }

    public static <T> SupplierHolder<T> of(String tag, Supplier<T> supplier) {
        return new SupplierHolder<>(tag, supplier);
    }

    public static <T> SupplierHolder<T> of(String tag, Supplier<T> supplier, boolean enableLog) {
        return new SupplierHolder<>(tag, supplier, enableLog);
    }

    public static <T> SupplierHolder<T> of(String tag, Supplier<T> supplier, Function<Object, String> logText) {
        return new SupplierHolder<>(tag, supplier, logText);
    }

    public SupplierHolder(Supplier<T> supplier) {
        this.tag = null;
        this.supplier = supplier;
        this.logText = Objects::toString;
    }

    public SupplierHolder(String tag, Supplier<T> supplier) {
        this.tag = tag;
        this.supplier = supplier;
        this.logText = Objects::toString;
    }

    public SupplierHolder(String tag, Supplier<T> supplier, boolean enableLog) {
        this.tag = tag;
        this.supplier = supplier;
        this.logText = Objects::toString;
        this.enableLog = enableLog;
    }

    public SupplierHolder(String tag, Supplier<T> supplier, Function<Object, String> logText) {
        this.tag = tag;
        this.supplier = supplier;
        this.logText = logText;
    }

    private boolean checkEnableLog() {
        if (enableLog != null) {
            return enableLog;
        }
        if (enableLogFromProperty == null) {
            synchronized(SupplierHolder.class) {
                if (enableLogFromProperty == null) {
                    try {
                        if (System.getProperties().containsKey(PROPERTY_KEY)) {
                            enableLogFromProperty = Boolean.parseBoolean(System.getProperty(PROPERTY_KEY));
                            log.info("load enableLog={} from system properties", System.getProperty(PROPERTY_KEY));
                        } else {
                            try {
                                final Properties properties = CommonUtil.readFromClasspathFile(PROP_FILE_NAME);
                                if (properties != null && properties.containsKey(PROPERTY_KEY)) {
                                    enableLogFromProperty = Boolean.parseBoolean(properties.getProperty(PROPERTY_KEY));
                                    log.info("load enableLog={} from {}",
                                            properties.getProperty(PROPERTY_KEY), PROP_FILE_NAME);
                                }
                            } catch (Exception e) {
                                // nothing
                            }
                        }
                    } catch (Exception e) {
                        // nothing
                    }
                    if (enableLogFromProperty == null) {
                        enableLogFromProperty = true; // 默认开启
                    }
                }
            }
        }
        enableLog = enableLogFromProperty;
        return enableLog;
    }

    @Override
    public T get() {
        if (this.target == null) {
            try {
                this.target = this.supplier.get();
            } finally {
                if (checkEnableLog() && this.tag != null) {
                    log.info("{} => {}", this.tag, this.logText.apply(this.target));
                }
            }
        }
        return this.target;
    }

    public Supplier<T> rawSupplier() {
        return this.supplier;
    }

    public T forceGet() {
        return this.supplier.get();
    }

    public T getCurrent() {
        return this.target;
    }

    public T replace(Supplier<T> supplier) {
        try {
            this.target = supplier.get();
        } finally {
            if (checkEnableLog() && this.tag != null) {
                log.info("{} => {}", this.tag, this.logText.apply(this.target));
            }
        }
        return this.target;
    }

    public T refresh() {
        return replace(this.supplier);
    }

}
