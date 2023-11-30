package com.event.bus.rocketmq.spring.boot.sample.product;

import com.event.bus.rocketmq.boot.annotation.EventBusProducer;
import com.event.bus.rocketmq.boot.core.EventBusRocketMQTemplate;

/**
 * @author : wh
 * @date : 2023/11/30 09:45
 * @description:
 */
@EventBusProducer(
    nameServer = "${event.bus.rocketmq.name-server}",
    topic = "${event.bus.rocketmq.topic}",
    groupId = "${event.bus.rocketmq.order.producer.groupId}"
)
public class OrderProducer extends EventBusRocketMQTemplate {
}
