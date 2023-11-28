package com.event.bus.rocketmq.boot.core;

import org.springframework.core.ResolvableType;

/**
 * @author : wh
 * @date : 2023/11/27 18:11
 * @description:
 */
public interface EventBusGenericApplicationListener extends EventBusMessageListener<EventBusAbstractMessage> {

    boolean supportsEventType(ResolvableType eventType);

    boolean supportsEventType(String tag);

    void onApplicationEvent(EventBusAbstractMessage eventBusAbstractMessage);
}