package com.event.bus.rocketmq.boot.core;

import com.aliyun.openservices.ons.api.Message;
import java.util.Collection;

/**
 * @author : wh
 * @date : 2023/11/27 18:05
 * @description:
 */
public interface EventBusMessageMulticaster {
    boolean multicastMessage(Message message, String uniqueConsumerId);

    void addMessageListeners(Collection<EventBusMessageListener<?>> listeners, String uniqueConsumerId);
}
