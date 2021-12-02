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

    private static ClassLoader classLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

    public static Properties readInputStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }

    public static Properties readFromClasspathFile(String fileName) throws IOException {
        try (InputStream inputStream = classLoader().getResourceAsStream(fileName)) {
            return readInputStream(inputStream);
        }
    }

}
