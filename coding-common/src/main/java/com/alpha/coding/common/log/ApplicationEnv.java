package com.alpha.coding.common.log;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

import org.springframework.beans.factory.InitializingBean;

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
public class ApplicationEnv implements InitializingBean {

    private String system;
    private String appName;
    private String host;
    private String port;
    private String module;
    private String pid;
    private Map<String, String> extraMap;

    public ApplicationEnv() {
        this.host = getLocalHostName();
        this.port = getLocalPort();
        this.pid = getCurrentPid();
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

    private String getLocalPort() {
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

    private String getCurrentPid() {
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            return jvmName.split("@")[0];
        } catch (Throwable e) {
            log.error("getCurrentPid fail", e);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        initSystemProperties();
    }

    private void putIfAbsentToSystemProperty(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value == null ? "" : value);
        }
    }
}
