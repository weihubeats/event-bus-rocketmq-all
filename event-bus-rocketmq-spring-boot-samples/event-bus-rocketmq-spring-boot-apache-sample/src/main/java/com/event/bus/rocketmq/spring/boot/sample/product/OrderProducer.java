package com.event.bus.rocketmq.spring.boot.sample.product;

import com.event.bus.rocketmq.boot.annotation.EventBusProducer;
import com.event.bus.rocketmq.boot.core.EventBusRocketMQTemplate;
import com.event.bus.rocketmq.spring.boot.sample.MQConstant;

/**
 * @author : wh
 * @date : 2023/11/30 09:45
 * @description:
 */
@EventBusProducer(topic = MQConstant.TOPIC)
public class OrderProducer extends EventBusRocketMQTemplate {
}
