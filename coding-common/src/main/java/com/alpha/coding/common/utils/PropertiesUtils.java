package com.alpha.coding.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

/**
 * PropertiesUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class PropertiesUtils {

    private static final String XML_FILE_EXTENSION = ".xml";

    public static Properties readPropertiesFile(String propertiesFileName) throws IOException {
        if (propertiesFileName == null) {
            return null;
        }
        InputStream is = getResourceAsStream(propertiesFileName);
        try {
            return readPropertiesFile(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
    }

    public static Properties readPropertiesFile(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }

    public static InputStream getResourceAsStream(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    public static Properties loadProperties(String resourceName, ClassLoader classLoader) throws IOException {
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = ClassUtils.getDefaultClassLoader();
        }
        Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
                ClassLoader.getSystemResources(resourceName));
        Properties props = new Properties();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection con = url.openConnection();
            ResourceUtils.useCachesIfNecessary(con);
            InputStream is = con.getInputStream();
            try {
                if (resourceName.endsWith(XML_FILE_EXTENSION)) {
                    props.loadFromXML(is);
                } else {
                    props.load(is);
                }
            } finally {
                is.close();
            }
        }
        return props;
    }

}
