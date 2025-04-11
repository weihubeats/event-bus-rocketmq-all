## event-bus-rocketmq-all
基于RocketMQ二次封装的领域事件,支持tag级别消息分发，基于redis天然消息消费幂等

## 功能
- 支持注解方式收发消息
- 支持基于tag级别的消息转发
- 基于redis进行幂等处理
- 支持apache rocketmq 消息收发
- 支持多集群消息发送消费
- 支持消息消费失败飞书告警
- 支持ons rocketmq 消息收发(待处理)
- 支持混合云消息消费(开源和ons) todo

## 使用

### 开源RocketMQ消息收发

参考 [event-bus-rocketmq-spring-boot-apache-sample](event-bus-rocketmq-spring-boot-samples%2Fevent-bus-rocketmq-spring-boot-apache-sample)

1. 引入依赖

```xml
        <dependency>
            <groupId>io.github.weihubeats</groupId>
            <artifactId>event-bus-rocketmq-apache-boot-starter</artifactId>
            <version>0.0.1</version>
        </dependency>
```
2. MQ配置

```yaml
event:
  bus:
    domain: event.bus.xiaozou.com:80
    subgroup: nsaddr-1
```

1. 支持基于`domian`和`subgroup`的http方式配置获取Nameserve地址
2. 也可以手动指定Nameserve地址


### 消息发送

- 继承 EventBusRocketMQTemplate

默认不配置`nameServer`地址，则使用全局配置`nameServer`地址,即`event.bus.domain`和`event.bus.subgroup`,`topic`也是

```java
@EventBusProducer
public class OrderProducer extends EventBusRocketMQTemplate {
}
```

如果需要手动配置成其他`topic`或者`nameServer`地址，可以自己配置
```java
@EventBusProducer(
    apacheNameServer = "${event.bus.rocketmq.name-server:}",
    topic = "${event.bus.rocketmq.pay.topic:}",
    groupId = "${event.bus.rocketmq.pay.producer.groupId:}"
)
public class OrderProducer extends EventBusRocketMQTemplate {
}
```
- 消息发送

直接注入`OrderProducer`即可

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
        orderProducer.sendMessage(msg);

    }
    
}
```


### 消息消费

- 使用全局配置的MQ配置消费消息仅需指定topic和gid即可

```java
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
```

- 消费其他MQ集群消息


通过`@EventBusConsumer`指定Nameserve地址即可
