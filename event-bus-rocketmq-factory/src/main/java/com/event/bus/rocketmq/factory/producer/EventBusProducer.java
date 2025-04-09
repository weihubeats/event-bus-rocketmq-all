package com.event.bus.rocketmq.factory.producer;

import com.event.bus.rocketmq.factory.EventBusMQException;
import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.EventBusMessageQueueSelector;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface EventBusProducer {

    EventBusSendResult send(EventBusMessage message, EventBusMessageQueueSelector selector, Object key);

    void send(EventBusMessage msg, EventBusSendCallback sendCallback) throws EventBusMQException;

    void sendOneway(final EventBusMessage message);

    EventBusSendResult send(final EventBusMessage message);

    void start();

    void shutdown();

}
