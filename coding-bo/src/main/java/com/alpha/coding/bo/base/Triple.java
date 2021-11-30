package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Triple
 *
 * @param <F> first
 * @param <S> second
 * @param <T> third
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Triple<F, S, T> implements Serializable {

    private F f;
    private S s;
    private T t;

    public static <F, S, T> Triple<F, S, T> of(F f, S s, T t) {
        return new Triple<F, S, T>(f, s, t);
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
        Triple other = (Triple) obj;
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
        if (t == null) {
            if (other.t != null) {
                return false;
            }
        } else if (!t.equals(other.t)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        long result = f != null ? f.hashCode() : 0;
        result = 31 * result + (s != null ? s.hashCode() : 0);
        result = 31 * result + (t != null ? t.hashCode() : 0);
        return (int) result;
    }

}
