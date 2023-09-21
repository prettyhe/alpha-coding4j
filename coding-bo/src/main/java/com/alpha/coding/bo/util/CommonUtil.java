package com.alpha.coding.bo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * CommonUtil
 *
 * @version 1.0
 * Date: 2021/12/1
 */
public class CommonUtil {

    /**
     * 获取默认的ClassLoader
     * <ul>
     * <li>优先取当前线程上下文的ClassLoader</li>
     * <li>其次取当前工具类的ClassLoader</li>
     * <li>再次取系统ClassLoader</li>
     * </ul>
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader
        }
        if (cl == null) {
            cl = CommonUtil.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader
                }
            }
        }
        return cl;
    }

    /**
     * 将输入流读取到Properties中
     *
     * @param is 输入流
     */
    public static Properties readInputStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }

    /**
     * 将类路径文件读取到Properties中
     *
     * @param fileName 文件名
     */
    public static Properties readFromClasspathFile(String fileName) throws IOException {
        return readFromClasspathFile(getDefaultClassLoader(), fileName);
    }

    /**
     * 将类路径文件读取到Properties中
     *
     * @param classLoader 类加载器
     * @param fileName    文件名
     */
    public static Properties readFromClasspathFile(ClassLoader classLoader, String fileName) throws IOException {
        if (classLoader == null) {
            try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName)) {
                return readInputStream(inputStream);
            }
        } else {
            try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
                return readInputStream(inputStream);
            }
        }
    }

}
