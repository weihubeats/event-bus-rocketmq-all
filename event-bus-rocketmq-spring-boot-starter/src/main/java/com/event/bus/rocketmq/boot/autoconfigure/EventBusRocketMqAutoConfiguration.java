package com.event.bus.rocketmq.boot.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author : wh
 * @date : 2023/11/24 16:23
 * @description:
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EventBusRocketMQProperties.class)
@RequiredArgsConstructor

public class EventBusRocketMqAutoConfiguration {

    private final Environment environment;
}
