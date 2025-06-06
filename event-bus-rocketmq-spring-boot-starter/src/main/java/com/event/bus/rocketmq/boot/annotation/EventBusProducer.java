package com.event.bus.rocketmq.boot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * @author : wh
 * @date : 2023/11/24 16:21
 * @description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EventBusProducer {

    String TOPIC = "${event.bus.product.topic:}";

    String GROUP_ID = "${event.bus.product.group.id:}";

    /**
     * apache NameServer
     */
    String APACHE_NAMESERV_ADDR = "${event.bus.rocketmq.apache.nameServer:}";

    /**
     * ONS NameServer
     */
    String ONS_NAMESERV_ADDR = "${event.bus.rocketmq.ons.nameServer:}";

    String apacheNameServer() default APACHE_NAMESERV_ADDR;

    String onsNameServer() default ONS_NAMESERV_ADDR;

    /**
     * bean Name
     *
     * @return
     */
    String beanName() default "";

    String topic() default TOPIC;

    String groupId() default GROUP_ID;

}
