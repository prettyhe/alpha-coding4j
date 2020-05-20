/**
 * Copyright
 */
package com.alpha.coding.common.bean.comm;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.alpha.coding.bo.assist.ref.InjectRef;
import com.alpha.coding.bo.assist.ref.InjectRefType;

import lombok.Setter;

/**
 * InjectSelfRefBeanProcessor
 *
 * <p>
 * 对bean初始化的一个拦截，处理{@link AbstractSelfRefBean}类bean的初始化
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class InjectSelfRefBeanProcessor implements BeanPostProcessor, ApplicationContextAware,
        ApplicationListener<ContextClosedEvent> {

    @Setter
    private ApplicationContext applicationContext;

    /**
     * 继承{@link AbstractSelfRefBean}的子类使用了嵌套代理，需要特殊处理
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractSelfRefBean) {
            if (AopUtils.isAopProxy(bean)) {
                // 如果当前对象是AOP代理对象，直接注入
                ((AbstractSelfRefBean) bean).setSelf((AbstractSelfRefBean) bean);
            } else {
                // 如果当前对象不是AOP代理，则通过applicationContext.getBean(beanName)获取代理对象并注入
                // 此种方式不适合解决prototype Bean的代理对象注入
                ((AbstractSelfRefBean) bean).setSelf((AbstractSelfRefBean) applicationContext.getBean(beanName));
            }
        }

        if (bean instanceof SelfRefBean) {
            if (AopUtils.isAopProxy(bean)) {
                SelfRefBeanDelegator.register(beanName, ((SelfRefBean) bean).self(), (SelfRefBean) bean);
            } else {
                SelfRefBeanDelegator.register(beanName, (SelfRefBean) bean,
                        (SelfRefBean) applicationContext.getBean(beanName));
            }
        }

        if (bean instanceof InjectRef) {
            if (AopUtils.isAopProxy(bean)) {
                InjectRefType.INTERFACE.getContextVisitor().visit().register(AopUtils.getTargetClass(bean), bean);
                InjectRefType.GLOBAL.getContextVisitor().visit().register(AopUtils.getTargetClass(bean), bean);
            } else {
                InjectRefType.INTERFACE.getContextVisitor().visit().register(bean.getClass(),
                        applicationContext.getBean(beanName));
                InjectRefType.GLOBAL.getContextVisitor().visit().register(bean.getClass(),
                        applicationContext.getBean(beanName));
            }
        }
        // @InjectRef 注解的bean
        final Class beanClass = AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
        final Set<Class> beanAllClassSet = new LinkedHashSet<>();
        findTypes(beanClass, beanAllClassSet);
        beanAllClassSet.stream()
                .filter(p -> p.isAnnotationPresent(com.alpha.coding.bo.assist.ref.anno.InjectRef.class))
                .findAny()
                .ifPresent(x -> {
                    InjectRefType.ANNOTATION.getContextVisitor().visit().register(beanClass, bean);
                    InjectRefType.GLOBAL.getContextVisitor().visit().register(beanClass, bean);
                });

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (contextClosedEvent.getApplicationContext().getParent() == null) {
            Arrays.stream(InjectRefType.values()).forEach(x -> Optional
                    .ofNullable(x.getContextVisitor().visit()).ifPresent(c -> c.clear()));
        }
    }

    private void findTypes(Class beanClass, Set<Class> set) {
        set.add(beanClass);
        final Class superclass = beanClass.getSuperclass();
        if (superclass != null && !superclass.getName().startsWith("java.")
                && !superclass.getName().startsWith("javax.")) {
            findTypes(superclass, set);
        }
        Optional.ofNullable(beanClass.getInterfaces())
                .ifPresent(p -> Arrays.stream(p).forEach(i -> findTypes(i, set)));
    }

}
