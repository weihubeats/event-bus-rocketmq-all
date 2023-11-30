## event-bus-rocketmq-all
基于RocketMQ二次封装的领域事件,支持tag级别消息分发，基于redis天然消息消费幂等

## 使用

参考 [event-bus-rocketmq-spring-boot-sample](event-bus-rocketmq-spring-boot-samples%2Fevent-bus-rocketmq-spring-boot-sample)

### 消息发送

- 继承 EventBusRocketMQTemplate 配置Nameserve、topic。 可以配置多个不同的MQ
```java
@EventBusProducer(
    nameServer = "${event.bus.rocketmq.name-server}",
    topic = "${event.bus.rocketmq.topic}",
    groupId = "${event.bus.rocketmq.order.producer.groupId}"
)
public class OrderProducer extends EventBusRocketMQTemplate {
}
```

```java
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
```

如果想向其他topic发送消息，可以再配置生产者
```java
@EventBusProducer(
nameServer = "${event.bus.rocketmq.name-server}",
topic = "${event.bus.rocketmq.pay.topic}",
groupId = "${event.bus.rocketmq.pay.producer.groupId}"
)
public class PayProducer extends EventBusRocketMQTemplate {
}
```

### 消息消费

支持tag级别的消息转发
```java
@EventBusConsumer(groupId = "${event.bus.order.consumer.groupId}", nameServer = "${event.bus.order.nameServer}", topic = "$event.bus.order.topic}")
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
```

