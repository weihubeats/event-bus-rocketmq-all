package com.event.bus.rocketmq.apache;

import com.event.bus.rocketmq.factory.EventBusClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/4/8
 * @description:
 */
@Configuration(proxyBeanMethods = false)
public class ApacheMQConfiguration {
    
    @Bean
    public EventBusClientFactory eventBusClientFactory() {
        return new ApacheEventBusClientFactory();
    }
    
    
}
