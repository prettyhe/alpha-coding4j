package com.alpha.coding.common.bean.identity;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.HashBasedTable;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * IdentityBeanFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class IdentityBeanFactory implements ApplicationContextAware, InitializingBean {

    private HashBasedTable<Class<? extends IdentityBean>, Object, IdentityBean> beanTable = HashBasedTable.create();
    private HashBasedTable<Class<?>, Object, Object> annaBeanTable = HashBasedTable.create();

    @Setter
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<String, IdentityBean> entry : applicationContext
                .getBeansOfType(IdentityBean.class).entrySet()) {
            final IdentityBean bean = entry.getValue();
            beanTable.put(bean.beanClz(), bean.identity().get(), bean);
        }
        for (Class<? extends IdentityBean> r : beanTable.rowKeySet()) {
            for (Map.Entry<Object, IdentityBean> entry : beanTable.row(r).entrySet()) {
                log.info("IdentityBean: clz={},id={},v={}",
                        r.getName(), entry.getKey(), entry.getValue().getClass().getName());
            }
        }
        initForAnnotationIdentityBean();
        for (Class<?> r : annaBeanTable.rowKeySet()) {
            for (Map.Entry<Object, Object> entry : annaBeanTable.row(r).entrySet()) {
                log.info("@IdentityBean: clz={},id={},v={}",
                        r.getName(), entry.getKey(), entry.getValue().getClass().getName());
            }
        }
    }

    private void initForAnnotationIdentityBean() {
        final Class<com.alpha.coding.common.bean.identity.annotation.IdentityBean> annotationType =
                com.alpha.coding.common.bean.identity.annotation.IdentityBean.class;
        for (Map.Entry<String, Object> entry : applicationContext
                .getBeansWithAnnotation(annotationType).entrySet()) {
            handleAnnotationIdentityBean(entry.getKey(), entry.getValue(), entry.getValue().getClass());
        }
    }

    private void handleAnnotationIdentityBean(String beanName, Object v, Class<?> clz) {
        if (clz == null) {
            return;
        }
        try {
            if (clz.isAnnotationPresent(com.alpha.coding.common.bean.identity.annotation.IdentityBean.class)) {
                final com.alpha.coding.common.bean.identity.annotation.IdentityBean annotation =
                        clz.getAnnotation(com.alpha.coding.common.bean.identity.annotation.IdentityBean.class);
                annaBeanTable.put(annotation.beanClass(),
                        v.getClass().getMethod(annotation.identityMethod()).invoke(v), v);
            }
            final Class<?> superclass = clz.getSuperclass();
            if (superclass != null && !superclass.equals(Object.class)) {
                handleAnnotationIdentityBean(beanName, v, superclass);
            }
            final Class<?>[] interFaces = clz.getInterfaces();
            if (interFaces != null && interFaces.length > 0) {
                for (Class<?> face : interFaces) {
                    if (!face.equals(Object.class)) {
                        handleAnnotationIdentityBean(beanName, v, face);
                    }
                }
            }
        } catch (Exception e) {
            log.error("handleAnnotationIdentityBean error for name={},bean={},clz={}",
                    beanName, v.getClass(), clz, e);
        }
    }

    private Class<?> findClassForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            log.info("{}", e.getMessage());
            return null;
        }
    }

    public <T> T getByIdentity(Class<T> clz, Object identity) {
        if (IdentityBean.class.isAssignableFrom(clz)) {
            return (T) beanTable.get(clz, identity);
        }
        if (clz.isAnnotationPresent(com.alpha.coding.common.bean.identity.annotation.IdentityBean.class)) {
            final Class<?> beanClass =
                    clz.getAnnotation(com.alpha.coding.common.bean.identity.annotation.IdentityBean.class).beanClass();
            return (T) annaBeanTable.get(beanClass, identity);
        }
        final T bean = (T) annaBeanTable.get(clz, identity);
        if (bean == null) {
            throw new IllegalArgumentException("Illegal Class " + clz.getSimpleName());
        }
        return bean;
    }

}
