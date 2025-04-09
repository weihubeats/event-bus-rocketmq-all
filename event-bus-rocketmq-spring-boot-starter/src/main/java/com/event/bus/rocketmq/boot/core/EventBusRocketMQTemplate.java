package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.boot.constants.EventBusMessageConstants;
import com.event.bus.rocketmq.boot.utils.JsonUtil;
import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.EventBusMessageQueueSelector;
import com.event.bus.rocketmq.factory.producer.EventBusProducer;
import com.event.bus.rocketmq.factory.producer.EventBusSendResult;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

/**
 * @author : wh
 * @date : 2023/11/29 09:59
 * @description:
 */
@Slf4j
public class EventBusRocketMQTemplate implements DisposableBean {

    private EventBusProducer producer;

    private String topic;

    private ExecutorService executorService;

    public void sendMessage(EventBusAbstractMessage abstractMessage) {
        sendMessage(abstractMessage, null, false, false);
    }

    /**
     * 发送延时消息
     *
     * @param abstractMessage
     * @param localDateTime
     */
    public void sendMessage(EventBusAbstractMessage abstractMessage, LocalDateTime localDateTime) {
        sendMessage(abstractMessage, localDateTime, false, false);
    }

    /**
     * 发送延时消息 by oneWay
     *
     * @param abstractMessage
     * @param localDateTime
     */
    public void sendMessageByOneway(EventBusAbstractMessage abstractMessage, LocalDateTime localDateTime) {
        sendMessage(abstractMessage, localDateTime, true, false);
    }

    /**
     * 发送分区有序消息
     *
     * @param abstractMessage
     * @param localDateTime
     */
    public void sendOrderedMessage(EventBusAbstractMessage abstractMessage, LocalDateTime localDateTime) {
        sendMessage(abstractMessage, localDateTime, false, true);
    }

    /**
     * 发送分区有序消息
     *
     * @param abstractMessage
     */
    public void sendOrderedMessage(EventBusAbstractMessage abstractMessage) {
        sendMessage(abstractMessage, null, false, true);
    }

    public void sendMessage(EventBusAbstractMessage abstractMessage, LocalDateTime localDateTime, boolean isOneway,
        boolean isOrder) {
        Map<String, Object> map = new HashMap<>();
        map.put(EventBusMessageConstants.EVENT_BUS_DATA, abstractMessage);
        map.put(EventBusMessageConstants.EVENT_MESSAGE_VERSION, abstractMessage.getVersion());
        String content = JsonUtil.toJSONString(map);
        // todo: 发包可考虑使用 class 分发消息而非tag
        EventBusMessage message = createMessage(abstractMessage, localDateTime, content);
        readyToSend(isOneway, isOrder, message, abstractMessage);
        log.info("发送消息 topic {} msgId {} message {}", topic, abstractMessage.getMsgId(), content);
    }

    private void readyToSend(boolean isOneway, boolean isOrder, EventBusMessage message,
        EventBusAbstractMessage abstractMessage) {
        if (isOrder) {
            Assert.isTrue(StringUtils.isNotBlank(message.getShardingKey()), "顺序消息必须提供shardingKey");
            EventBusMessageQueueSelector selector = (mqs, msg, shardingKey) -> {
                int select = Math.abs(shardingKey.hashCode());
                if (select < 0) {
                    select = 0;
                }
                return mqs.get(select % mqs.size());
            };
            EventBusSendResult send = producer.send(message, selector, message.getShardingKey());
            abstractMessage.setMsgId(send.getMsgId());
        } else if (isOneway) {
            producer.sendOneway(message);
            abstractMessage.setMsgId(message.getMsgID());
        } else {
            EventBusSendResult send = producer.send(message);
            abstractMessage.setMsgId(send.getMsgId());
        }
    }

    private EventBusMessage createMessage(EventBusAbstractMessage abstractMessage, LocalDateTime localDateTime,
        String jsonString) {
        EventBusMessage message = new EventBusMessage(topic, abstractMessage.getTag(), jsonString.getBytes(StandardCharsets.UTF_8));

        if (Objects.nonNull(localDateTime)) {
            message.setStartDeliverTime(localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli());
        }

        if (Objects.nonNull(abstractMessage.getKey())) {
            message.setKey(abstractMessage.getKey());
        }

        if (Objects.nonNull(abstractMessage.getShardingKey())) {
            message.setShardingKey(abstractMessage.getShardingKey());
        }
        return message;
    }

    public void setProducer(EventBusProducer producer) {
        this.producer = producer;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }

    }
}
