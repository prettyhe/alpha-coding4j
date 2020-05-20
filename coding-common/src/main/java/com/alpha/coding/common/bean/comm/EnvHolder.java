package com.alpha.coding.common.bean.comm;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * EnvHolder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class EnvHolder implements EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    public Environment getEnvironment() {
        return this.env;
    }

    public boolean containsProperty(String key) {
        return this.env.containsProperty(key);
    }

    public String getProperty(String key) {
        return this.env.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return this.env.getProperty(key, defaultValue);
    }

    public <T> T getProperty(String key, Class<T> clz) {
        return this.env.getProperty(key, clz);
    }

    public <T> T getProperty(String key, Class<T> clz, T defaultValue) {
        return this.env.getProperty(key, clz, defaultValue);
    }

}
