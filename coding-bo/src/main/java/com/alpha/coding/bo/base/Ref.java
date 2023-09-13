package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Ref
 *
 * @version 1.0
 * Date: 2023/9/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Ref<T> implements Serializable {

    private T t;

    public static <T> Ref<T> of(T t) {
        return new Ref<>(t);
    }

    public static <T> Ref<T> empty() {
        return new Ref<>();
    }

    public Ref<T> update(T t) {
        this.t = t;
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ref)) {
            return false;
        }
        Ref other = (Ref) obj;
        return (t == null && other.t == null) || (t != null && t.equals(other.t));
    }

    @Override
    public int hashCode() {
        return (t == null ? 0 : t.hashCode()) + 31 * this.getClass().hashCode();
    }

}
