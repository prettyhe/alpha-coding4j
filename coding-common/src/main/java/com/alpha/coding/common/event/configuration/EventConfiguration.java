package com.alpha.coding.common.event.configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.enums.util.EnumUtils;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.utils.StringUtils;
import com.google.common.eventbus.EventBus;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventConfiguration 事件配置
 * <p>
 * <li>identity: 事件类别，如com.example.event.test.event.OpEventType</li>
 * <li>eventIdentityClassName: 事件类别类名，如com.example.event.test.event.OpEventType</li>
 * <li>enablePublishEmptyEvent: 是否允许发布空事件，true/false</li>
 * <li>effectedEventTypes: 此配置生效的事件类型，不配置则升级为全局配置</li>
 * <li>allowedPublishEventTypes: 允许发布的事件类型，应为effectedEventTypes子集，不配置则该类事件下所有类型都有效</li>
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class EventConfiguration implements InitializingBean {

    public static final EventConfiguration DEFAULT = new EventConfiguration();

    // ------------------可配置项---------------------
    /**
     * 事件身份标识
     */
    @Setter
    @Getter
    private Class<? extends EnumWithCodeSupplier> identity;

    /**
     * 事件身份标识类名
     */
    @Setter
    @Getter
    private String eventIdentityClassName;

    /**
     * com.google.common.eventbus.EventBus 实例
     */
    @Setter
    @Getter
    private EventBus eventBusInstance;

    /**
     * 是否允许发布空事件
     */
    @Setter
    @Getter
    private boolean enablePublishEmptyEvent = false;

    /**
     * 此配置生效的事件类型，不配置则升级为全局配置
     */
    @Setter
    @Getter
    private String effectedEventTypes;

    /**
     * 允许发布的事件类型，不配置则该类事件下所有类型都有效
     */
    @Setter
    @Getter
    private String allowedPublishEventTypes;
    /**
     * 允许将配置的允许事件类型名解析成指定的事件类型，默认支持枚举类事件类型的解析
     */
    @Setter
    private Function<String, EnumWithCodeSupplier> stringToSpecifyEventFunc =
            t -> EnumUtils.safeParseEnumByName(identity, t);
    // ------------------预处理项---------------------
    @Getter
    private Set<EnumWithCodeSupplier> effectedEventTypeSet;
    @Getter
    private Set<EnumWithCodeSupplier> allowedPublishEventSet;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (identity == null) {
            identity = (Class<? extends EnumWithCodeSupplier>) Class.forName(eventIdentityClassName, true,
                    Thread.currentThread().getContextClassLoader());
        }
        final Function<String, Set<EnumWithCodeSupplier>> function = t -> Arrays.stream(t.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(stringToSpecifyEventFunc)
                .filter(p -> p != null)
                .collect(Collectors.toSet());
        if (StringUtils.isNotBlank(effectedEventTypes)) {
            effectedEventTypeSet = function.apply(effectedEventTypes);
        }
        if (StringUtils.isNotBlank(allowedPublishEventTypes)) {
            allowedPublishEventSet = function.apply(allowedPublishEventTypes);
        }
        if (log.isDebugEnabled()) {
            log.debug("init EventConfiguration: {}", JSON.toJSONString(this));
        }
    }

}
