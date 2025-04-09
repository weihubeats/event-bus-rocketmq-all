package com.event.bus.rocketmq.boot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * @author : wh
 * @date : 2023/11/24 16:17
 * @description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EventBusConsumer {

    String GROUP_ID = "${event.bus.rocketmq.consumer.groupId:}";
    
    String TOPIC = "${event.bus.rocketmq.consumer.topic:}";
    /**
     * 消费线程数
     */
    String CONSUMER_THREAD_NUMS = "${consumer.thread.nums:}";

    String MAX_RECONSUME_TIMES = "${consumer.max.reconsume.times:}";
    
    /**
     * apache NameServer
     */
    String APACHE_NAMESERV_ADDR = "${event.bus.rocketmq.apache.nameServer:}";

    /**
     * ONS NameServer
     */
    String ONS_NAMESERV_ADDR = "${event.bus.rocketmq.ons.nameServer:}";


    String topic() default TOPIC;

    String groupId() default GROUP_ID;

    String apacheNameServer() default APACHE_NAMESERV_ADDR;

    String onsNameServer() default ONS_NAMESERV_ADDR;
    


    /**
     * 消费线程数
     *
     * @return
     */
    String consumerThreadNums() default CONSUMER_THREAD_NUMS;

    /**
     * 最大重试次数
     * @return
     */
    String maxReconsumeTimes() default MAX_RECONSUME_TIMES;


    String beanName() default "";

    /**
     * 消费者id 默认 groupId + topic md5
     * @return
     */
    String consumerId() default "";


}
