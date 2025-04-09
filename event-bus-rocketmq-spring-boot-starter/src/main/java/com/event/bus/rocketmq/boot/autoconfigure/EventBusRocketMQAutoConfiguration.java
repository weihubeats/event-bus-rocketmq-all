package com.event.bus.rocketmq.boot.autoconfigure;

import com.event.bus.rocketmq.boot.annotation.EventBusConsumer;
import com.event.bus.rocketmq.boot.core.DefaultEventBusMessageListenerFactory;
import com.event.bus.rocketmq.boot.core.EventBusErrorHandler;
import com.event.bus.rocketmq.boot.core.EventBusSimpleEventMulticaster;
import com.event.bus.rocketmq.boot.exception.DefaultEventBusErrorHandler;
import com.event.bus.rocketmq.boot.storage.MethodSuccessStorage;
import com.event.bus.rocketmq.boot.storage.RedisMethodSuccessStorageImpl;
import com.event.bus.rocketmq.factory.EventBusClientFactory;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author : wh
 * @date : 2023/11/29 10:13
 * @description:
 */
@EnableConfigurationProperties(EventBusRocketMQProperties.class)
@Import({EventBusRocketMQPropertiesHolder.class, EventBusProducerRegisterAutoConfiguration.class, DefaultEventBusMessageListenerFactory.class, EventBusSimpleEventMulticaster.class})
@RequiredArgsConstructor
@Configuration
public class EventBusRocketMQAutoConfiguration {

    private final RedissonClient redissonClient;

    @ConditionalOnMissingBean(MethodSuccessStorage.class)
    @Bean
    public MethodSuccessStorage methodSuccessStorage() {
        return new RedisMethodSuccessStorageImpl(redissonClient);

    }

    @ConditionalOnMissingBean(EventBusErrorHandler.class)
    @Bean
    public EventBusErrorHandler eventBusErrorHandler(EventBusRocketMQProperties eventBusRocketMQProperties) {
        return new DefaultEventBusErrorHandler(eventBusRocketMQProperties);
    }


    /**
     * @param eventBusRocketMQPropertiesHolder
     * @return
     * {@link EventBusConsumer} 注解开关 没有配置默认是生效的，event.bus.consumer.enabled=false 则注解不生效。
     */
    @ConditionalOnProperty(name = "event.bus.consumer.enabled", matchIfMissing = true, havingValue = "true")
    @Bean
    public EventBusConsumerRegisterAutoConfiguration eventBusConsumerRegisterAutoConfiguration(EventBusRocketMQPropertiesHolder eventBusRocketMQPropertiesHolder, EventBusClientFactory eventBusClientFactory) {
        return new EventBusConsumerRegisterAutoConfiguration(eventBusRocketMQPropertiesHolder, eventBusClientFactory);
    }
}
