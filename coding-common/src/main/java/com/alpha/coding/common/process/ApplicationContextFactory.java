package com.alpha.coding.common.process;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ApplicationContextFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ApplicationContextFactory {

    public static AbstractApplicationContext create(Class<?>... annotatedClasses) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(annotatedClasses);
        ctx.refresh();
        return ctx;
    }

    public static AbstractApplicationContext create(String... configLocations) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocations);
        ctx.refresh();
        return ctx;
    }

}
