package com.event.bus.rocketmq.spring.boot.sample.product;

import com.event.bus.rocketmq.boot.annotation.EventBusProducer;
import com.event.bus.rocketmq.boot.core.EventBusRocketMQTemplate;

/**
 * @author : wh
 * @date : 2023/11/30 09:47
 * @description:
 */
@EventBusProducer(
    apacheNameServer = "${event.bus.rocketmq.name-server:}",
    topic = "${event.bus.rocketmq.pay.topic:}",
    groupId = "${event.bus.rocketmq.pay.producer.groupId:}"
)
public class PayProducer extends EventBusRocketMQTemplate {
}
