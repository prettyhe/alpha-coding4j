package com.alpha.coding.common.bean.comm;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

import lombok.Setter;

/**
 * PropertiesStringValueResolver
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Setter
public class PropertiesStringValueResolver implements EmbeddedValueResolverAware {

    private StringValueResolver embeddedValueResolver;

    /**
     * Resolve the given String value, for example parsing placeholders.
     *
     * @param key the original String value (never {@code null})
     *
     * @return the resolved String value (may be {@code null} when resolved to a null
     * value), possibly the original String value itself (in case of no placeholders
     * to resolve or when ignoring unresolvable placeholders)
     *
     * @throws IllegalArgumentException in case of an unresolvable String value
     */
    public String getProperty(String key) {
        return embeddedValueResolver.resolveStringValue("${" + key + "}");
    }

}
