package com.alpha.coding.common.aop;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AspectJParams
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Data
@Accessors(chain = true)
public class AspectJParams {

    private boolean proxyTargetClass;
    private String aopRefBean;
    private int aopOrder;
    private String aopAdvice;
    private String aopAdviceMethod;
    private String aopPointcut;

    public Map<String, Object> build() {
        Map<String, Object> map = new HashMap<>();
        map.put("enable-proxy-target-class", proxyTargetClass);
        map.put("aop-ref", aopRefBean);
        map.put("aop-order", aopOrder);
        map.put("aop-advice", aopAdvice);
        map.put("aop-advice-method", aopAdviceMethod);
        map.put("aop-pointcut", aopPointcut);
        return map;
    }
}
