package com.event.bus.rocketmq.spring.boot.sample.consumer;

import com.event.bus.rocketmq.boot.annotation.EventBusConsumer;
import com.event.bus.rocketmq.boot.annotation.EventBusListener;
import com.event.bus.rocketmq.boot.core.EventBusMessageListener;
import com.event.bus.rocketmq.spring.boot.sample.event.OrderEvent;

/**
 * @author : wh
 * @date : 2023/11/30 09:48
 * @description:
 */
@EventBusConsumer(groupId = "${event.bus.order.consumer.groupId:}", topic = "${event.bus.order.topic:}")
public class OrderConsumer implements EventBusMessageListener<OrderEvent> {

    @EventBusListener(tag = OrderEvent.TAG)
    public void test(OrderEvent orderEvent) {
        System.out.println("消费者1,消息" + orderEvent.getMsg());
    }

    @EventBusListener(tag = OrderEvent.TAG)
    public void test2(OrderEvent orderEvent) {
        System.out.println("消费者2,消息" + orderEvent.getMsg());
    }

}
