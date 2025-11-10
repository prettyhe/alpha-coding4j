package com.alpha.coding.common.log;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alpha.coding.common.bean.comm.ApplicationContextHolder;
import com.alpha.coding.common.utils.NetUtils;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationEnv
 *
 * @version 1.0
 */
@Slf4j
@Data
@Accessors(chain = true)
public class ApplicationEnv implements InitializingBean, ApplicationListener<ContextRefreshedEvent>, Ordered {

    private String system;
    private String appName;
    private String host;
    private String port;
    private String module;
    private String pid;
    private Map<String, String> extraMap;
    private ApplicationContext applicationContext;
    private Supplier<String> portSupplier = new SmartPortSupplier(Arrays.asList(this::getApplicationContext,
            ApplicationContextHolder::getCurrentApplicationContext));

    public ApplicationEnv() {
        this.host = NetUtils.getLocalHost();
        this.pid = getCurrentPid();
        this.port = portSupplier.get();
    }

    private String getCurrentPid() {
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            return jvmName.split("@")[0];
        } catch (Throwable e) {
            log.error("getCurrentPid fail", e);
        }
        return null;
    }

    private static String getLocalHttpPort() {
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            if (objectNames == null || objectNames.isEmpty()) {
                return null;
            }
            return objectNames.iterator().next().getKeyProperty("port");
        } catch (Exception e) {
            log.warn("find Local HTTP Port fail", e);
        }
        return null;
    }

    private static String getLocalDubboPort(ApplicationContext applicationContext) {
        // port for apache dubbo
        try {
            Integer port = null;
            if (ClassUtils.isPresent("org.apache.dubbo.config.ProtocolConfig", null)) {
                final Map<String, org.apache.dubbo.config.ProtocolConfig> beans =
                        applicationContext.getBeansOfType(org.apache.dubbo.config.ProtocolConfig.class);
                if (beans != null && !beans.isEmpty()) {
                    for (Map.Entry<String, org.apache.dubbo.config.ProtocolConfig> entry : beans.entrySet()) {
                        if (entry.getValue().getPort() != null) {
                            port = entry.getValue().getPort();
                            break;
                        }
                    }
                }
            }
            if (port != null) {
                return String.valueOf(port);
            }
            if (ClassUtils.isPresent("org.apache.dubbo.config.ServiceConfig", null)) {
                final Map<String, org.apache.dubbo.config.ServiceConfig> beans =
                        applicationContext.getBeansOfType(org.apache.dubbo.config.ServiceConfig.class);
                if (beans != null && !beans.isEmpty()) {
                    for (Map.Entry<String, org.apache.dubbo.config.ServiceConfig> entry : beans.entrySet()) {
                        final List<org.apache.dubbo.common.URL> exportedUrls = entry.getValue().getExportedUrls();
                        if (exportedUrls == null) {
                            continue;
                        }
                        for (org.apache.dubbo.common.URL exportedUrl : exportedUrls) {
                            port = exportedUrl.getPort();
                            if (port > 0) {
                                return String.valueOf(port);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.warn("find Apache Dubbo Port fail, {}", ex.getMessage());
        }
        // port for alibaba dubbo
        try {
            Integer port = null;
            if (ClassUtils.isPresent("com.alibaba.dubbo.config.ProtocolConfig", null)) {
                final Map<String, ProtocolConfig> beans = applicationContext.getBeansOfType(ProtocolConfig.class);
                if (beans != null && !beans.isEmpty()) {
                    for (Map.Entry<String, ProtocolConfig> entry : beans.entrySet()) {
                        if (entry.getValue().getPort() != null) {
                            port = entry.getValue().getPort();
                            break;
                        }
                    }
                }
            }
            if (port != null && port > 0) {
                return String.valueOf(port);
            }
            if (ClassUtils.isPresent("com.alibaba.dubbo.config.ServiceConfig", null)) {
                final Map<String, ServiceConfig> beans = applicationContext.getBeansOfType(ServiceConfig.class);
                if (beans != null && !beans.isEmpty()) {
                    for (Map.Entry<String, ServiceConfig> entry : beans.entrySet()) {
                        final List<URL> exportedUrls = entry.getValue().getExportedUrls();
                        if (exportedUrls == null) {
                            continue;
                        }
                        for (URL exportedUrl : exportedUrls) {
                            port = exportedUrl.getPort();
                            if (port > 0) {
                                return String.valueOf(port);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.warn("find Alibaba Dubbo Port fail, {}", ex.getMessage());
        }
        return null;
    }

    public void initSystemProperties() {
        putIfAbsentToSystemProperty("system", system);
        putIfAbsentToSystemProperty("appName", appName);
        putIfAbsentToSystemProperty("host", host);
        putIfAbsentToSystemProperty("port", port);
        putIfAbsentToSystemProperty("module", module);
        putIfAbsentToSystemProperty("pid", pid);
        if (extraMap != null) {
            for (Map.Entry<String, String> entry : extraMap.entrySet()) {
                putIfAbsentToSystemProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private void putIfAbsentToSystemProperty(String key, String value) {
        if ("".equals(System.getProperty(key, ""))) {
            System.setProperty(key, value == null ? "" : value);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initSystemProperties();
        log.info("ApplicationEnv is system={},appName={},host={},port={},module={},pid={},extraMap={}",
                system, appName, host, port, module, pid, extraMap);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.applicationContext = contextRefreshedEvent.getApplicationContext();
        this.port = this.portSupplier.get();
        if (this.port != null && !Objects.equals(this.port, System.getProperty("port"))) {
            System.setProperty("port", this.port);
            log.info("ApplicationEnv is system={},appName={},host={},port={},module={},pid={},extraMap={}",
                    system, appName, host, port, module, pid, extraMap);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * Http 端口获取
     */
    public static class HttpPortSupplier implements Supplier<String> {

        @Override
        public String get() {
            return getLocalHttpPort();
        }
    }

    /**
     * Dubbo 端口获取
     */
    public static class DubboPortSupplier implements Supplier<String>, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        @Override
        public String get() {
            return getLocalDubboPort(this.applicationContext);
        }
    }

    /**
     * 智能端口获取，优先检测当前环境的dubbo端口，再检测http端口
     */
    public static class SmartPortSupplier implements Supplier<String> {

        private final List<Supplier<ApplicationContext>> supplierList;

        public SmartPortSupplier(List<Supplier<ApplicationContext>> supplierList) {
            this.supplierList = supplierList;
        }

        @Override
        public String get() {
            String dubboPort = null;
            if (supplierList != null) {
                for (Supplier<ApplicationContext> supplier : supplierList) {
                    final ApplicationContext applicationContext = supplier.get();
                    if (applicationContext != null) {
                        dubboPort = getLocalDubboPort(applicationContext);
                        if (dubboPort != null) {
                            return dubboPort;
                        }
                    }
                }
            }
            return getLocalHttpPort();
        }
    }

}
