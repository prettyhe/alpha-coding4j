package com.alpha.coding.common.log;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
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

import com.alibaba.dubbo.config.ProtocolConfig;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationEnv
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Data
@Accessors(chain = true)
public class ApplicationEnv implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    private String system;
    private String appName;
    private String host;
    private String port;
    private String module;
    private String pid;
    private Map<String, String> extraMap;
    private Supplier<String> portSupplier = new HttpPortSupplier();

    public ApplicationEnv() {
        this.host = getLocalHostName();
        this.pid = getCurrentPid();
        this.port = portSupplier.get();
    }

    private String getLocalHostName() {
        final InetAddress address = getLocalHostLANAddress();
        return address == null ? null : address.getHostName();
    }

    private InetAddress getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
            // 遍历所有的网络接口
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) networkInterfaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) { // 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            log.error("getLocalHostLANAddress fail", e);
        }
        return null;
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
            if (objectNames == null || objectNames.size() == 0) {
                return null;
            }
            return objectNames.iterator().next().getKeyProperty("port");
        } catch (Exception e) {
            log.error("getLocalPort fail", e);
        }
        return null;
    }

    private static String getLocalDubboPort(ApplicationContext applicationContext) {
        try {
            if (Class.forName("com.alibaba.dubbo.config.ProtocolConfig") != null) {
                final Map<String, ProtocolConfig> beans = applicationContext.getBeansOfType(ProtocolConfig.class);
                Integer port = null;
                if (beans != null && !beans.isEmpty()) {
                    for (Map.Entry<String, ProtocolConfig> entry : beans.entrySet()) {
                        if (entry.getValue().getPort() != null) {
                            port = entry.getValue().getPort();
                            break;
                        }
                    }
                }
                if (port != null) {
                    return String.valueOf(port);
                }
            }
        } catch (Exception ex) {
            log.error("getDubboPort fail, {}", ex.getMessage());
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
        this.port = portSupplier.get();
        putIfAbsentToSystemProperty("port", this.port);
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

}
