package com.alpha.coding.common.bean.register;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * ApplicationPostListener
 *
 * @version 1.0
 * @date 2021年05月10日
 */
public class DefaultApplicationPostListener
        implements ApplicationPostListener, ApplicationListener<ContextRefreshedEvent> {

    private List<Runnable> postCallbacks = new ArrayList<>();

    @Override
    public void registerPostCallback(Runnable runnable) {
        if (runnable != null) {
            postCallbacks.add(runnable);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        postCallbacks.forEach(Runnable::run);
    }

}
