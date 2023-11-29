package com.event.bus.rocketmq.boot.core;

import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @author : wh
 * @date : 2023/11/29 10:31
 * @description:
 */
@Configuration
public class DefaultEventBusMessageListenerFactory implements EventBusMessageListenerFactory, Ordered {

    @Override
    public boolean supportsMethod(Method method) {
        return true;
    }

    @Override
    public EventBusMessageListener<?> createMessageListener(String beanName, String tag, Class<?> type, Method method, ApplicationContext applicationContext) {
        return new EventBusApplicationListenerMethodAdapter(beanName, tag, type, method, applicationContext);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}