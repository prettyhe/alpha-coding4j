package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.beanutils.ConvertUtils;

import com.alpha.coding.bo.annotation.XLSLabel;
import com.alpha.coding.common.utils.FieldUtils;
import com.alpha.coding.common.utils.convert.DateConverter;
import com.alpha.coding.common.utils.convert.NumberConverter;

/**
 * XLSOperator
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public abstract class XLSOperator {

    static {
        Stream.of(Long.class, Integer.class, BigDecimal.class)
                .forEach(type -> {
                    ConvertUtils.deregister(type);
                    ConvertUtils.register(NumberConverter.getDefault(), type);
                });
        ConvertUtils.deregister(Date.class);
        ConvertUtils.register(new DateConverter(), Date.class);
    }

    public static Map<Field, XLSLabelContext> fieldLabelMap(Class<?> clazz) {
        final List<Field> fields = FieldUtils.findMatchedFields(clazz, null);
        Map<Field, XLSLabelContext> map = new HashMap<>();
        for (Field field : fields) {
            final XLSLabel xlsLabel = field.getAnnotation(XLSLabel.class);
            if (xlsLabel != null) {
                map.put(field, new XLSLabelContext(xlsLabel));
                continue;
            }
            final Label label = field.getAnnotation(Label.class);
            if (label != null) {
                map.put(field, new XLSLabelContext(label));
            }
        }
        return map;
    }

}
