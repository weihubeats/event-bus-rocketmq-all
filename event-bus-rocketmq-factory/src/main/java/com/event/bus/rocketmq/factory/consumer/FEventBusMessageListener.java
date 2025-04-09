package com.event.bus.rocketmq.factory.consumer;

import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.status.EventBusAction;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface FEventBusMessageListener {

    EventBusAction consume(final EventBusMessage message, final EventBusConsumeContext context);
    
}
