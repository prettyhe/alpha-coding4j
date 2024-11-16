package com.alpha.coding.common.bean.comm;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import com.alpha.coding.bo.assist.ref.InjectRef;
import com.alpha.coding.bo.assist.ref.InjectRefType;
import com.alpha.coding.bo.assist.ref.RefContext;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class InjectSelfRefBeanProcessor implements BeanPostProcessor, ApplicationContextAware,
        ApplicationListener<ApplicationContextEvent>, Ordered {

    @Setter
    private ApplicationContext applicationContext;

    private void findTypes(Class<?> beanClass, Set<Class<?>> set) {
        set.add(beanClass);
        final Class<?> superclass = beanClass.getSuperclass();
        if (superclass != null && !superclass.getName().startsWith("java.")
                && !superclass.getName().startsWith("javax.")) {
            findTypes(superclass, set);
        }
        Optional.ofNullable(beanClass.getInterfaces())
                .ifPresent(p -> Arrays.stream(p).forEach(i -> findTypes(i, set)));
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        log.info("start process InjectSelfRefBean on listen " + applicationContextEvent);
        if (applicationContextEvent.getApplicationContext().getParent() != null) {
            return;
        }
        if (applicationContextEvent instanceof ContextRefreshedEvent) {
            applicationContext.getBeansOfType(AbstractSelfRefBean.class).forEach((beanName, bean) -> {
                if (AopUtils.isAopProxy(bean)) {
                    // 如果当前对象是AOP代理对象，直接注入
                    bean.setSelf(bean);
                } else {
                    // 如果当前对象不是AOP代理，则通过applicationContext.getBean(beanName)获取代理对象并注入
                    // 此种方式不适合解决prototype Bean的代理对象注入
                    bean.setSelf((AbstractSelfRefBean) applicationContext.getBean(beanName));
                }
            });
            applicationContext.getBeansOfType(SelfRefBean.class).forEach((beanName, bean) -> {
                if (AopUtils.isAopProxy(bean)) {
                    SelfRefBeanDelegator.register(beanName, bean.self(), bean);
                } else {
                    SelfRefBeanDelegator.register(beanName, bean,
                            (SelfRefBean) applicationContext.getBean(beanName));
                }
            });
            applicationContext.getBeansOfType(InjectRef.class).forEach((beanName, bean) -> {
                if (AopUtils.isAopProxy(bean)) {
                    InjectRefType.INTERFACE.getContextVisitor().visit().register(AopUtils.getTargetClass(bean), bean);
                    InjectRefType.GLOBAL.getContextVisitor().visit().register(AopUtils.getTargetClass(bean), bean);
                } else {
                    InjectRefType.INTERFACE.getContextVisitor().visit().register(bean.getClass(),
                            applicationContext.getBean(beanName));
                    InjectRefType.GLOBAL.getContextVisitor().visit().register(bean.getClass(),
                            applicationContext.getBean(beanName));
                }
            });
            applicationContext.getBeansWithAnnotation(com.alpha.coding.bo.assist.ref.anno.InjectRef.class)
                    .forEach((beanName, bean) -> {
                        final Class<?> beanClass = AopUtils.getTargetClass(bean);
                        InjectRefType.ANNOTATION.getContextVisitor().visit().register(beanClass, bean);
                        InjectRefType.GLOBAL.getContextVisitor().visit().register(beanClass, bean);
                    });
        } else if (applicationContextEvent instanceof ContextClosedEvent) {
            Arrays.stream(InjectRefType.values()).forEach(x -> Optional
                    .ofNullable(x.getContextVisitor().visit()).ifPresent(RefContext::clear));
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
