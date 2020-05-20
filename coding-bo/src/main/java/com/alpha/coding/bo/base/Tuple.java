/**
 * Copyright
 */
package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Tuple
 *
 * @param <F> first
 * @param <S> second
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Tuple<F, S> implements Serializable {

    private F f;
    private S s;

    public static <F, S> Tuple<F, S> of(F f, S s) {
        return new Tuple<F, S>(f, s);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple other = (Tuple) obj;
        if (f == null) {
            if (other.f != null) {
                return false;
            }
        } else if (!f.equals(other.f)) {
            return false;
        }
        if (s == null) {
            if (other.s != null) {
                return false;
            }
        } else if (!s.equals(other.s)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        long result = f != null ? f.hashCode() : 0;
        result = 31 * result + (s != null ? s.hashCode() : 0);
        return (int) result;
    }
}
