package com.event.bus.rocketmq.apache;

import com.event.bus.rocketmq.factory.EventBusClientFactory;
import com.event.bus.rocketmq.factory.EventBusPropertyKeyConst;
import com.event.bus.rocketmq.factory.consumer.EventBusConsumer;
import com.event.bus.rocketmq.factory.producer.EventBusProducer;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;
import org.springframework.util.Assert;

import static org.apache.rocketmq.remoting.netty.NettySystemConfig.COM_ROCKETMQ_REMOTING_CLIENT_CLOSE_SOCKET_IF_TIMEOUT;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class ApacheEventBusClientFactory implements EventBusClientFactory {

    private static final int SEND_MSG_TIMEOUT = 5000;

    @Override
    public EventBusProducer createProducer(Properties properties) {
        System.setProperty(COM_ROCKETMQ_REMOTING_CLIENT_CLOSE_SOCKET_IF_TIMEOUT, "false");
        boolean msgTrace = enableMsgTrace(properties);
        DefaultMQProducer producer = new DefaultMQProducer(null, msgTrace, null);
        producer.addRetryResponseCode(RemotingSysResponseCode.SYSTEM_BUSY);
        if (properties.containsKey(EventBusPropertyKeyConst.SendMsgTimeoutMillis)) {
            producer.setSendMsgTimeout((Integer.parseInt(properties.get(EventBusPropertyKeyConst.SendMsgTimeoutMillis).toString())));
        } else {
            producer.setSendMsgTimeout(SEND_MSG_TIMEOUT);
        }

        if (properties.getProperty(EventBusPropertyKeyConst.GROUP_ID) != null) {
            producer.setProducerGroup(properties.getProperty(EventBusPropertyKeyConst.GROUP_ID));
        }
        if (!ObjectUtils.isEmpty(properties.getProperty(EventBusPropertyKeyConst.APACHE_NAMESRV_ADDR))) {
            producer.setNamesrvAddr(properties.getProperty(EventBusPropertyKeyConst.APACHE_NAMESRV_ADDR));
        } else {
            String domain = properties.getProperty(EventBusPropertyKeyConst.DOMAIN);
            String subgroup = properties.getProperty(EventBusPropertyKeyConst.SUBGROUP);
            Assert.notNull(domain, "event bus rocketmq nameServer [domain] address must not be empty");
            Assert.notNull(subgroup, "event bus rocketmq nameServer [subgroup] address must not be empty");
            System.setProperty("rocketmq.namesrv.domain", domain);
            System.setProperty("rocketmq.namesrv.domain.subgroup", subgroup);
        }
        return ApacheMQConverter.toEventBusProducer(producer);
    }

    @Override
    public EventBusConsumer createConsumer(Properties properties) {
        DefaultMQPushConsumer consumer = createDefaultConsumer(properties);
        return new ApacheConsumer(consumer);
    }

    private DefaultMQPushConsumer createDefaultConsumer(Properties properties) {
        System.setProperty(COM_ROCKETMQ_REMOTING_CLIENT_CLOSE_SOCKET_IF_TIMEOUT, "false");
        // 如果有灰度 tag, 那么在 group_id 后加上后缀
        String consumerGroup = properties.getProperty(EventBusPropertyKeyConst.GROUP_ID);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup, enableMsgTrace(properties), null);

        String namsesrvAddr = properties.getProperty(EventBusPropertyKeyConst.APACHE_NAMESRV_ADDR);

        if (!org.springframework.util.ObjectUtils.isEmpty(namsesrvAddr)) {
            consumer.setNamesrvAddr(namsesrvAddr);
        } else {
            String domain = properties.getProperty(EventBusPropertyKeyConst.DOMAIN);
            String subgroup = properties.getProperty(EventBusPropertyKeyConst.SUBGROUP);
            System.setProperty("rocketmq.namesrv.domain", domain);
            System.setProperty("rocketmq.namesrv.domain.subgroup", subgroup);
        }

        setConsumerProperties(consumer, DefaultMQPushConsumer::setMaxReconsumeTimes, Integer.class, properties, EventBusPropertyKeyConst.MaxReconsumeTimes);
        setConsumerProperties(consumer, DefaultMQPushConsumer::setConsumeThreadMax, Integer.class, properties, EventBusPropertyKeyConst.ConsumeThreadMax);
        setConsumerProperties(consumer, DefaultMQPushConsumer::setConsumeThreadMin, Integer.class, properties, EventBusPropertyKeyConst.ConsumeThreadMin);
        setConsumerProperties(consumer, DefaultMQPushConsumer::setConsumeTimeout, Long.class, properties, EventBusPropertyKeyConst.ConsumeTimeout);
        setConsumerProperties(consumer, DefaultMQPushConsumer::setConsumeTimestamp, String.class, properties, EventBusPropertyKeyConst.ConsumeTimestamp);

        return consumer;
    }

    private <V> void setConsumerProperties(DefaultMQPushConsumer consumer,
        BiConsumer<DefaultMQPushConsumer, V> setConsumer, Class<V> tClass, Properties properties, String key) {
        Object value = properties.get(key);
        if (!org.springframework.util.ObjectUtils.isEmpty(properties) && tClass.isInstance(value)) {
            setConsumer.accept(consumer, tClass.cast(value));
        }
    }

    private boolean enableMsgTrace(Properties properties) {
        boolean enableMsgTrace = false;
        Object msgTraceSwitch = properties.get(EventBusPropertyKeyConst.MsgTraceSwitch);
        if (Objects.nonNull(msgTraceSwitch) && msgTraceSwitch instanceof Boolean) {
            enableMsgTrace = (boolean) msgTraceSwitch;
        }
        return enableMsgTrace;
    }

}
