package com.event.bus.rocketmq.boot.annotation;

import com.event.bus.rocketmq.boot.autoconfigure.EventBusRocketMqAutoConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author : wh
 * @date : 2023/11/24 16:23
 * @description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(EventBusRocketMqAutoConfiguration.class)
public @interface EnableEventBus {
}
