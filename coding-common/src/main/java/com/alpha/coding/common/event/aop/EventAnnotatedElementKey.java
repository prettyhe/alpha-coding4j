package com.alpha.coding.common.event.aop;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.util.ObjectUtils;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.extern.slf4j.Slf4j;

/**
 * EventAnnotatedElementKey
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Slf4j
public class EventAnnotatedElementKey implements Comparable<EventAnnotatedElementKey> {

    private final AnnotatedElementKey annotatedElementKey;

    private final Class<? extends EnumWithCodeSupplier> eventClass;

    private final String type;

    public EventAnnotatedElementKey(AnnotatedElementKey annotatedElementKey,
                                    Class<? extends EnumWithCodeSupplier> eventClass, String type) {
        this.annotatedElementKey = annotatedElementKey;
        this.eventClass = eventClass;
        this.type = type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EventAnnotatedElementKey)) {
            return false;
        }
        EventAnnotatedElementKey otherKey = (EventAnnotatedElementKey) other;
        return (this.annotatedElementKey.equals(otherKey.annotatedElementKey)
                        && ObjectUtils.nullSafeEquals(this.eventClass, otherKey.eventClass)
                        && ObjectUtils.nullSafeEquals(this.type, otherKey.type));
    }

    @Override
    public int hashCode() {
        long result = this.annotatedElementKey != null ? this.annotatedElementKey.hashCode() : 0;
        result = 31 * result + (this.eventClass != null ? this.eventClass.hashCode() : 0);
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return (int) result;
    }

    @Override
    public String toString() {
        return (this.annotatedElementKey == null ? "" : this.annotatedElementKey.toString())
                + " ## " + (this.eventClass == null ? "" : this.eventClass.getName())
                + " ## " + (this.type == null ? "" : this.type);
    }

    @Override
    public int compareTo(EventAnnotatedElementKey o) {
        int result = this.annotatedElementKey.toString().compareTo(o.annotatedElementKey.toString());
        if (result == 0 && this.eventClass != null) {
            result = this.eventClass.getName().compareTo(o.eventClass == null ? "" : o.eventClass.getName());
        }
        if (result == 0 && this.type != null) {
            result = this.type.compareTo(o.type == null ? "" : o.type);
        }
        return result;
    }
}
