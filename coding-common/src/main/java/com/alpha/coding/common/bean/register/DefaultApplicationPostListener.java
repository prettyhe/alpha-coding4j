package com.alpha.coding.common.bean.register;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import com.alpha.coding.common.utils.ClassUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationPostListener
 *
 * @version 1.0
 * @date 2021年05月10日
 */
@Slf4j
public class DefaultApplicationPostListener
        implements ApplicationPostListener, ApplicationListener<ContextRefreshedEvent>, Ordered {

    public static final String BEAN_NAME = "InternalApplicationPostListener";

    private final List<Runnable> postCallbacks = new ArrayList<>();

    @Override
    public void registerPostCallback(Runnable runnable) {
        if (runnable != null) {
            log.info("[{}]注入回调", ClassUtils.getCallerCallerClassName());
            postCallbacks.add(runnable);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("应用刷新回调:{}", postCallbacks.size());
        postCallbacks.forEach(Runnable::run);
    }

    @Override
    public int getOrder() {
        return -200;
    }

}
