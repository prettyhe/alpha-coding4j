package com.alpha.coding.bo.function;

/**
 * Converter
 *
 * @version 1.0
 * Date: 2021/1/24
 */
public interface Converter<T> {

    /**
     * Convert the specified input object into an output object of the
     * specified type.
     *
     * @param type  Data type to which this value should be converted
     * @param value The input value to be converted
     * @return The converted value
     */
    T convert(Class<T> type, Object value);

}
