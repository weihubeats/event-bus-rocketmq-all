package com.event.bus.rocketmq.boot.autoconfigure;

import com.event.bus.rocketmq.boot.annotation.EventBusProducer;
import com.event.bus.rocketmq.boot.core.EventBusRocketMQTemplate;
import com.event.bus.rocketmq.factory.EventBusClientFactory;
import com.event.bus.rocketmq.factory.EventBusPropertyKeyConst;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ObjectUtils;

/**
 * @author : wh
 * @date : 2023/11/29 09:58
 * @description:
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class EventBusProducerRegisterAutoConfiguration implements ApplicationContextAware, SmartInitializingSingleton, EnvironmentAware {

    private ApplicationContext applicationContext;

    private final EventBusRocketMQPropertiesHolder eventBusRocketMQPropertiesHolder;

    private final EventBusClientFactory eventBusClientFactory;

    private StandardEnvironment environment;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(EventBusProducer.class)
            .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        beans.forEach(this::registerProduct);
    }

    private void registerProduct(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
        if (!EventBusRocketMQTemplate.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + EventBusRocketMQTemplate.class.getName());
        }
        EventBusProducer annotation = clazz.getAnnotation(EventBusProducer.class);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        validate(annotation, genericApplicationContext);
        com.event.bus.rocketmq.factory.producer.EventBusProducer producer = createProducer(annotation);
        try {
            producer.start();
            log.info("producer start, beanName {}", beanName);
        } catch (Exception e) {
            throw new BeanDefinitionValidationException(String.format("Failed to startup EventBusProducer for EventBusRocketMQTemplate %s",
                beanName), e);
        }
        EventBusRocketMQTemplate eventBusRocketMQTemplate = (EventBusRocketMQTemplate) bean;
        eventBusRocketMQTemplate.setProducer(producer);
        eventBusRocketMQTemplate.setTopic(eventBusRocketMQPropertiesHolder.bindPropertyGlobal(annotation.topic(), EventBusRocketMQProperties.Producer::getTopic, eventBusRocketMQPropertiesHolder.getProducer()));
        log.info("Set real producer to {} {}", beanName, annotation.beanName());
    }

    private com.event.bus.rocketmq.factory.producer.EventBusProducer createProducer(EventBusProducer annotation) {
        Properties properties = new Properties();

        EventBusRocketMQProperties.Producer producer = eventBusRocketMQPropertiesHolder.getProducer();

        String aliMQAccessKey = eventBusRocketMQPropertiesHolder.getAliMQAccessKey();

        String aliMQSecretKey = eventBusRocketMQPropertiesHolder.getAliMQSecretKey();

        if (!ObjectUtils.isEmpty(aliMQAccessKey)) {
            properties.put(EventBusPropertyKeyConst.AccessKey, aliMQAccessKey);
        }
        if (!ObjectUtils.isEmpty(aliMQSecretKey)) {
            properties.put(EventBusPropertyKeyConst.SecretKey, aliMQSecretKey);
        }

        String domain = eventBusRocketMQPropertiesHolder.getDomain();
        String subgroup = eventBusRocketMQPropertiesHolder.getSubgroup();
        if (!ObjectUtils.isEmpty(domain)) {
            properties.put(EventBusPropertyKeyConst.DOMAIN, domain);
        }
        if (!ObjectUtils.isEmpty(subgroup)) {
            properties.put(EventBusPropertyKeyConst.SUBGROUP, subgroup);
        }

        String onsNameserver = eventBusRocketMQPropertiesHolder.bindPropertyGlobal(annotation.onsNameServer(), EventBusRocketMQProperties.Producer::getOnsNameServer, producer);

        if (!ObjectUtils.isEmpty(onsNameserver)) {
            properties.put(EventBusPropertyKeyConst.ONS_NAMESRV_ADDR, onsNameserver);
        }
        // apache NameServe
        String apacheNameserver = eventBusRocketMQPropertiesHolder.bindPropertyGlobal(annotation.apacheNameServer(), EventBusRocketMQProperties.Producer::getApacheNameServer, producer);

        if (!ObjectUtils.isEmpty(apacheNameserver)) {
            properties.put(EventBusPropertyKeyConst.APACHE_NAMESRV_ADDR, apacheNameserver);
        }

        properties.put(EventBusPropertyKeyConst.MsgTraceSwitch, eventBusRocketMQPropertiesHolder.getEnableMsgTrace());

        return eventBusClientFactory.createProducer(properties);
    }

    private void validate(EventBusProducer annotation, GenericApplicationContext genericApplicationContext) {
        if (genericApplicationContext.isBeanNameInUse(annotation.beanName())) {
            throw new BeanDefinitionValidationException(String.format("Bean {} has been used in Spring Application Context, " +
                "please check the @EventBusProducer", annotation.beanName()));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }
}
