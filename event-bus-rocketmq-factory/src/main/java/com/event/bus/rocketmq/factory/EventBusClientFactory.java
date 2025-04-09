package com.event.bus.rocketmq.factory;

import com.event.bus.rocketmq.factory.consumer.EventBusConsumer;
import com.event.bus.rocketmq.factory.producer.EventBusProducer;
import java.util.Properties;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface EventBusClientFactory {

    EventBusProducer createProducer(final Properties properties);

    EventBusConsumer createConsumer(final Properties properties);

}
