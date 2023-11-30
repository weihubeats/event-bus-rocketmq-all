package com.event.bus.rocketmq.spring.boot.sample.controller;

import com.event.bus.rocketmq.spring.boot.sample.event.OrderEvent;
import com.event.bus.rocketmq.spring.boot.sample.product.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2023/11/30 09:51
 * @description:
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final OrderProducer orderProducer;

    @GetMapping("/order/send")
    public void orderSendMsg() {
        OrderEvent msg = new OrderEvent();
        msg.setKey("111test");
        msg.setShardingKey("111");
        msg.setMsg("hello world");
        msg.setMsgId("111");
        orderProducer.sendMessage(msg);

    }
    
}
