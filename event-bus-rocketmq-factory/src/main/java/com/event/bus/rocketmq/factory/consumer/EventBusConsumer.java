package com.event.bus.rocketmq.factory.consumer;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface EventBusConsumer {

    void start();

    void shutdown();

    void subscribe(final String topic, final String subExpression, final FEventBusMessageListener listener);

    void subscribeConcurrently(final String topic, final String subExpression,
        final EventBusMessageListenerConcurrently listener);

    void subscribe(final String topic, final EventBusMessageSelector selector, final FEventBusMessageListener listener);
    
}
