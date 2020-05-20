package com.alpha.coding.common.bean.comm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.support.AopUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SelfRefBeanDelegator
 *
 * @version 1.0
 * Date: 2020-02-27
 */
@Data
@AllArgsConstructor
public class SelfRefBeanDelegator {

    private static final Map<SelfRefBean, SelfRefBeanDelegator> BEAN_CACHE = new HashMap<>();

    private String beanName;
    private SelfRefBean proxyBean;

    public static SelfRefBean lookup(SelfRefBean bean) {
        return AopUtils.isAopProxy(bean) ? bean
                : BEAN_CACHE.get(bean) == null ? null : BEAN_CACHE.get(bean).getProxyBean();
    }

    public static void register(String beanName, SelfRefBean bean, SelfRefBean proxyBean) {
        BEAN_CACHE.put(bean, new SelfRefBeanDelegator(beanName, proxyBean));
    }

}
