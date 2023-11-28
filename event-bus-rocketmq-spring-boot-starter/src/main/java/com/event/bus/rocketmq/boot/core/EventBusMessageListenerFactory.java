package com.event.bus.rocketmq.boot.core;

import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;

/**
 * @author : wh
 * @date : 2023/11/27 17:57
 * @description:
 */
public interface EventBusMessageListenerFactory {

    boolean supportsMethod(Method method);

    EventBusMessageListener<?> createMessageListener(String beanName, String tag, Class<?> type, Method method, ApplicationContext applicationContext);

}
