package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.consumer.EventBusConsumeContext;
import com.event.bus.rocketmq.factory.consumer.FEventBusMessageListener;
import com.event.bus.rocketmq.factory.status.EventBusAction;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author : wh
 * @date : 2023/11/27 18:28
 * @description:
 */
@Slf4j
public class EventBusAbstractMessagePublisher implements FEventBusMessageListener {

    @Nullable
    private final EventBusMessageMulticaster eventBusMessageMulticaster;

    private final String groupTopicConsumerId;

    public EventBusAbstractMessagePublisher(EventBusMessageMulticaster multicaster, String groupTopicConsumerId) {
        this.eventBusMessageMulticaster = multicaster;
        this.groupTopicConsumerId = groupTopicConsumerId;
    }

    public boolean publishMessage(EventBusMessage message) {
        Assert.notNull(message, "message must not be null");
        if (this.eventBusMessageMulticaster == null) {
            throw new IllegalStateException("EventBusMessageMulticaster not initialized");
        }
        return eventBusMessageMulticaster.multicastMessage(message, this.groupTopicConsumerId);
    }

    @Override
    public EventBusAction consume(EventBusMessage message, EventBusConsumeContext context) {
        String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
        String msgID = message.getMsgID();
        log.info("收到消息 id {} body {} tag {}", msgID, msgBody, message.getTag());
        if (publishMessage(message)) {
            return EventBusAction.CommitMessage;
        } else {
            log.error("message {} 执行失败,等待重试 重试次数 {}", msgID, message.getReconsumeTimes());
            return EventBusAction.ReconsumeLater;
        }
    }

}

