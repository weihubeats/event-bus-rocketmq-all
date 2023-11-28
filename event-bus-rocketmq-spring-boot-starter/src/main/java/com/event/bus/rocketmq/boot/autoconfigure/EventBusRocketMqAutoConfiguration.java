package com.event.bus.rocketmq.boot.autoconfigure;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.event.bus.rocketmq.boot.annotation.EventBusConsumer;
import com.event.bus.rocketmq.boot.annotation.EventBusListener;
import com.event.bus.rocketmq.boot.core.EventBusAbstractMessagePublisher;
import com.event.bus.rocketmq.boot.core.EventBusApplicationListenerMethodAdapter;
import com.event.bus.rocketmq.boot.core.EventBusConsumerHolder;
import com.event.bus.rocketmq.boot.core.EventBusMessageListener;
import com.event.bus.rocketmq.boot.core.EventBusMessageListenerFactory;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author : wh
 * @date : 2023/11/24 16:23
 * @description:
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EventBusRocketMQProperties.class)
@Import({EventBusRocketMQPropertiesHolder.class})
@RequiredArgsConstructor
@Slf4j
public class EventBusRocketMqAutoConfiguration implements ApplicationContextAware, SmartInitializingSingleton, EnvironmentAware, DisposableBean {

    private StandardEnvironment environment;

    private ApplicationContext applicationContext;

    private final Set<Consumer> consumers = new CopyOnWriteArraySet<>();

    /**
     * key  consumerId value: method
     */
    private final Map<String, Set<EventBusMessageListener<?>>> messageListeners = new ConcurrentHashMap<>();

    private final Set<String> consumersSet = new CopyOnWriteArraySet<>();

    private final EventBusRocketMQPropertiesHolder eventBusRocketMQPropertiesHolder;

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    @Nullable
    private List<EventBusMessageListenerFactory> messageListenerFactories;

    @Override
    public void afterSingletonsInstantiated() {
        if (!eventBusRocketMQPropertiesHolder.isConsumerFlag()) {
            log.info("Consumer flag is false, so no consumer is registered");
            return;
        }
        Map<String, EventBusMessageListenerFactory> factories = this.applicationContext.getBeansOfType(EventBusMessageListenerFactory.class, false, false);
        if (!factories.isEmpty()) {
            this.messageListenerFactories = new ArrayList<>(factories.size());
            this.messageListenerFactories.addAll(factories.values());
        }

        Map<String, Object> beans = this.applicationContext
            .getBeansWithAnnotation(EventBusConsumer.class)
            .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        findSubscribers(beans);

        // register consumers
        beans.forEach(this::registerConsumer);
    }

    private void registerConsumer(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
        EventBusConsumer annotation = clazz.getAnnotation(EventBusConsumer.class);
        // 缓存
        if (consumersSet.contains(consumerId(annotation))) {
            // unique consumer
            return;
        }
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        validate(annotation, genericApplicationContext);

        // listeners for consumerId
        Set<EventBusMessageListener<?>> listeners = messageListeners.get(consumerId(annotation));

        // create consumer but not subscribe
        Consumer consumer = createConsumer(annotation);
        // Save to the spring container for destruction use
        consumers.add(consumer);
        // register consumer bean
        genericApplicationContext.registerBean(consumerId(annotation), EventBusConsumerHolder.class, () -> new EventBusConsumerHolder(consumer));

        // create EventBusAbstractMessagePublisher
        initAbstractMessagePublisher(consumer, listeners, annotation);

        // put consumer to be unique
        consumersSet.add(consumerId(annotation));

        try {
            consumer.start();
            log.info("event bus rocketMQ Consumer {} startup successfully", consumerId(annotation));
        } catch (Exception e) {
            throw new BeanDefinitionValidationException(String.format("Failed to startup EventBusConsumer for %s", annotation.groupId()), e);
        }
    }

    private void findSubscribers(Map<String, Object> beans) {
        beans.forEach((beanName, target) -> {
            Class<?> clazz = AopProxyUtils.ultimateTargetClass(target);
            EventBusConsumer annotation = clazz.getAnnotation(EventBusConsumer.class);
            Set<EventBusMessageListener<?>> subscribers = findSubscribers(beanName, clazz);
            String consumerId = consumerId(annotation);
            if (messageListeners.containsKey(consumerId)) {
                messageListeners.get(consumerId).addAll(subscribers);
            } else {
                messageListeners.put(consumerId, subscribers);
            }
        });
    }

    private void initAbstractMessagePublisher(Consumer consumer, Set<EventBusMessageListener<?>> listeners,
        EventBusConsumer annotation) {
        // todo support custom multicaster
        // get bean from spring ioc
        EventBusSimpleEventMulticaster multicaster = this.applicationContext.getBean(EventBusSimpleEventMulticaster.class);

        String groupTopicConsumerId = consumerId(annotation);

        // set messageListeners according to consumer because we only have one multicaster
        multicaster.addMessageListeners(listeners, groupTopicConsumerId);
        String topic = eventBusRocketMQPropertiesHolder.bindProperty(annotation.topic(), EventBusRocketMQPropertiesHolder::getTopic, eventBusRocketMQPropertiesHolder);
        EventBusAbstractMessagePublisher abstractMessagePublisher = new EventBusAbstractMessagePublisher(multicaster, groupTopicConsumerId);
        consumer.subscribe(topic, "*", abstractMessagePublisher);
    }

    private Consumer createConsumer(EventBusConsumer annotation) {
        EventBusRocketMQProperties.Consumer propertiesConsumer = eventBusRocketMQPropertiesHolder.getConsumer();
        Properties properties = new Properties();

        properties.put(PropertyKeyConst.GROUP_ID, eventBusRocketMQPropertiesHolder.bindProperty(annotation.groupId(), EventBusRocketMQProperties.Consumer::getGroupId, propertiesConsumer));
        properties.put(PropertyKeyConst.AccessKey, eventBusRocketMQPropertiesHolder.getAliMQAccessKey());
        properties.put(PropertyKeyConst.SecretKey, eventBusRocketMQPropertiesHolder.getAliMQSecretKey());
        properties.put(PropertyKeyConst.NAMESRV_ADDR, eventBusRocketMQPropertiesHolder.bindPropertyGlobal(annotation.nameServer(), EventBusRocketMQProperties.Consumer::getNameServer, propertiesConsumer));
        properties.put(PropertyKeyConst.ConsumeThreadNums, eventBusRocketMQPropertiesHolder.bindPropertyByInt(annotation.consumerThreadNums(), EventBusRocketMQProperties.Consumer::getConsumerThreadNums, propertiesConsumer));
        properties.put(PropertyKeyConst.MaxReconsumeTimes, eventBusRocketMQPropertiesHolder.bindPropertyByInt(annotation.maxReconsumeTimes(), EventBusRocketMQProperties.Consumer::getMaxReconsumeTimes, propertiesConsumer));
        return ONSFactory.createConsumer(properties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }

    private void validate(EventBusConsumer annotation,
        GenericApplicationContext genericApplicationContext) {
        if (genericApplicationContext.isBeanNameInUse(annotation.beanName())) {
            throw new BeanDefinitionValidationException(
                String.format("Bean %s has been used in Spring Application Context, " +
                        "please check the @EventBusConsumer",
                    annotation.beanName()));
        }
    }

    private String consumerId(EventBusConsumer ann) {
        String customerId = ann.consumerId();
        if (ObjectUtils.isEmpty(customerId)) {
            byte[] consumerIdByte = String.join("", ann.topic(), ann.groupId()).getBytes(StandardCharsets.UTF_8);
            return DigestUtils.md5DigestAsHex(consumerIdByte);
        }
        return customerId;

    }

    private Set<EventBusMessageListener<?>> findSubscribers(final String beanName, final Class<?> targetType) {

        if (validationAnnotation(targetType)) {
            Map<Method, EventBusListener> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(targetType,
                    (MethodIntrospector.MetadataLookup<EventBusListener>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, EventBusListener.class));
            } catch (Throwable ex) {
                // An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
                if (log.isDebugEnabled()) {
                    log.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
                }
            }

            if (CollectionUtils.isEmpty(annotatedMethods)) {
                this.nonAnnotatedClasses.add(targetType);
                if (log.isTraceEnabled()) {
                    log.trace("No @EventBusSubscriber annotations found on bean class: " + targetType.getName());
                }
            } else {
                // Non-empty set of methods
                ApplicationContext context = this.applicationContext;
                Assert.state(context != null, "No ApplicationContext set");
                List<EventBusMessageListenerFactory> factories = this.messageListenerFactories;
                Assert.state(factories != null, "EventBusMessageListenerFactory List not initialized");
                Set<EventBusMessageListener<?>> listeners = new HashSet<>();

                annotatedMethods.forEach(((method, eventBusListener) -> {
                    for (EventBusMessageListenerFactory factory : factories) {
                        if (factory.supportsMethod(method)) {
                            Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
                            // todo: 是否考虑直接使用className作为tag优化使用？
                            String tag = eventBusListener.tag();
                            EventBusMessageListener<?> applicationListener =
                                factory.createMessageListener(beanName, tag, targetType, methodToUse, applicationContext);
                            if (applicationListener instanceof EventBusApplicationListenerMethodAdapter) {
                                EventBusApplicationListenerMethodAdapter applicationListenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) applicationListener;
                                log.info("event bus beanName {} methodName {}", beanName, applicationListenerMethodAdapter.getMethod().getName());
                                listeners.add(applicationListener);
                            }
                            break;
                        }
                    }
                }));
                if (log.isDebugEnabled()) {
                    log.debug(annotatedMethods.size() + " @EventBusSubscriber methods processed on bean '" +
                        beanName + "': " + annotatedMethods);
                }
                return listeners;
            }
        }
        return Collections.emptySet();
    }

    /**
     * 兼容低版本 spring boot
     *
     * @param targetType
     * @return
     */
    private boolean validationAnnotation(Class<?> targetType) {
        boolean nonAnnotatedClassesFlag = !this.nonAnnotatedClasses.contains(targetType);
        boolean isCandidateClassFlag = ClassUtils.hasMethod(AnnotationUtils.class, "isCandidateClass", Class.class, Class.class) ?
            AnnotationUtils.isCandidateClass(targetType, EventBusListener.class) : !isSpringContainerClass(targetType);
        return nonAnnotatedClassesFlag && isCandidateClassFlag;
    }

    @Override
    public void destroy() {
        for (Consumer consumer : consumers) {
            if (!ObjectUtils.isEmpty(consumer)) {
                log.info("evnet bus rocketmq consumer shutdown");
                consumer.shutdown();
            }
        }
    }

    private static boolean isSpringContainerClass(Class<?> clazz) {
        return (clazz.getName().startsWith("org.springframework.") &&
            !AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
    }
}
