package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.factory.EventBusMessage;
import java.util.Collection;

/**
 * @author : wh
 * @date : 2023/11/27 18:05
 * @description:
 */
public interface EventBusMessageMulticaster {
    
    boolean multicastMessage(EventBusMessage message, String uniqueConsumerId);

    void addMessageListeners(Collection<EventBusMessageListener<?>> listeners, String uniqueConsumerId);
}
