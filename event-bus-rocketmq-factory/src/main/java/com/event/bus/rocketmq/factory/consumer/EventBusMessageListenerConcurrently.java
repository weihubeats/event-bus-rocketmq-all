package com.event.bus.rocketmq.factory.consumer;

import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.status.EventBusConsumeConcurrentlyStatus;
import java.util.List;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface EventBusMessageListenerConcurrently {

    EventBusConsumeConcurrentlyStatus consumeMessage(List<EventBusMessage> messages,
        EventBusConsumeConcurrentlyContext context);

}
