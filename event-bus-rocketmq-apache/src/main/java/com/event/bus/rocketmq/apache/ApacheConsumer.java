package com.event.bus.rocketmq.apache;

import com.event.bus.rocketmq.factory.EventBusMQException;
import com.event.bus.rocketmq.factory.consumer.EventBusConsumer;
import com.event.bus.rocketmq.factory.consumer.FEventBusMessageListener;
import com.event.bus.rocketmq.factory.consumer.EventBusMessageListenerConcurrently;
import com.event.bus.rocketmq.factory.consumer.EventBusMessageSelector;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;

/**
 * @author : wh
 * @date : 2025/4/8
 * @description:
 */
@RequiredArgsConstructor
public class ApacheConsumer implements EventBusConsumer {

    private final DefaultMQPushConsumer consumer;

    @Override
    public void start() {
        try {
            this.consumer.start();
        } catch (Exception e) {
            throw new EventBusMQException(e);
        }

    }

    @Override
    public void shutdown() {
        this.consumer.shutdown();
    }

    @Override
    public void subscribe(String topic, String subExpression, FEventBusMessageListener listener) {
        try {
            consumer.subscribe(topic, subExpression);
            consumer.registerMessageListener(ApacheMQConverter.toRocketMQListener(listener));
        }
        catch (MQClientException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void subscribeConcurrently(String topic, String subExpression,
        EventBusMessageListenerConcurrently listener) {

    }

    @Override
    public void subscribe(String topic, EventBusMessageSelector selector,
        FEventBusMessageListener listener) {

    }
}
