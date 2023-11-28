package com.event.bus.rocketmq.boot.autoconfigure;

import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ObjectUtils;

/**
 * @author : wh
 * @date : 2023/11/27 17:39
 * @description:
 */
@RequiredArgsConstructor
public class EventBusRocketMQPropertiesHolder implements EnvironmentAware {

    private StandardEnvironment environment;

    private final EventBusRocketMQProperties rocketMQProperties;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }

    public String bindProperty(String propertyName) {
        return bindProperty(propertyName, environment);
    }

    public <T> String bindProperty(String propertyName, Function<T, String> function, T t) {
        String property = bindProperty(propertyName, environment);
        return ObjectUtils.isEmpty(property) ? function.apply(t) : property;
    }

    /**
     * 获取全局配置 优先级: 注解 > 属性 > 全局
     *
     * @param propertyName
     * @param function
     * @param t
     * @param <T>
     * @return
     */
    public <T> String bindPropertyGlobal(String propertyName, Function<T, String> function, T t) {
        String property = bindProperty(propertyName, environment);
        return ObjectUtils.isEmpty(property) && Objects.nonNull(t) ? function.apply(t) : property;
    }

    public <T> Integer bindPropertyByInt(String propertyName, Function<T, Integer> function, T t) {
        String property = bindProperty(propertyName, environment);
        if (ObjectUtils.isEmpty(property) && Objects.nonNull(t)) {
            function.apply(t);
        }
        return ObjectUtils.isEmpty(property) ? null : Integer.parseInt(property);
    }

    public static String bindProperty(String propertyName, Environment environment) {
        return environment.resolvePlaceholders(propertyName);
    }

    public String getAliMQAccessKey() {
        return rocketMQProperties.getAliMQAccessKey();
    }

    public String getAliMQSecretKey() {
        return rocketMQProperties.getAliMQSecretKey();
    }

    public String getNameServer() {
        return rocketMQProperties.getAliYunNameServer();
    }

    public EventBusRocketMQProperties.Consumer getConsumer() {
        return rocketMQProperties.getConsumer();
    }

    public EventBusRocketMQProperties.Producer getProducer() {
        return rocketMQProperties.getProducer();
    }

    public String getTopic() {
        return rocketMQProperties.getTopic();
    }

    public String getDomain() {
        return rocketMQProperties.getDomain();
    }

    public String getSubgroup() {
        return rocketMQProperties.getSubgroup();
    }

    /**
     * 是否注册消费者
     *
     * @return
     */
    public boolean isConsumerFlag() {
        return rocketMQProperties.isConsumerFlag();
    }

    /**
     * 消息轨迹
     *
     * @return
     */
    public boolean getEnableMsgTrace() {
        return rocketMQProperties.isEnableMsgTrace();
    }

}

