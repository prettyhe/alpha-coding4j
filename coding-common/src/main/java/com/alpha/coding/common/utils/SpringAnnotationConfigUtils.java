package com.alpha.coding.common.utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * SpringAnnotationConfigUtils
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public class SpringAnnotationConfigUtils {

    public static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
                                                                    Class<?> containerClass,
                                                                    Class<?> annotationClass) {
        return attributesForRepeatable(metadata, containerClass.getName(), annotationClass.getName());
    }

    @SuppressWarnings("unchecked")
    private static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
                                                                     String containerClassName,
                                                                     String annotationClassName) {

        Set<AnnotationAttributes> result = new LinkedHashSet<AnnotationAttributes>();
        addAttributesIfNotNull(result, metadata.getAnnotationAttributes(annotationClassName, false));

        Map<String, Object> container = metadata.getAnnotationAttributes(containerClassName, false);
        if (container != null && container.containsKey("value")) {
            for (Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get("value")) {
                addAttributesIfNotNull(result, containedAttributes);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static void addAttributesIfNotNull(Set<AnnotationAttributes> result, Map<String, Object> attributes) {
        if (attributes != null) {
            result.add(AnnotationAttributes.fromMap(attributes));
        }
    }

}
