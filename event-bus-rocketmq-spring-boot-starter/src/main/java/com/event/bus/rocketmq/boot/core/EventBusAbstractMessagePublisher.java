package com.event.bus.rocketmq.boot.core;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
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
public class EventBusAbstractMessagePublisher implements MessageListener {

    @Nullable
    private final EventBusMessageMulticaster eventBusMessageMulticaster;

    private final String groupTopicConsumerId;

    public EventBusAbstractMessagePublisher(EventBusMessageMulticaster multicaster, String groupTopicConsumerId) {
        this.eventBusMessageMulticaster = multicaster;
        this.groupTopicConsumerId = groupTopicConsumerId;
    }

    public boolean publishMessage(Message message) {
        Assert.notNull(message, "message must not be null");
        if (this.eventBusMessageMulticaster == null) {
            throw new IllegalStateException("EventBusMessageMulticaster not initialized");
        }
        return eventBusMessageMulticaster.multicastMessage(message, this.groupTopicConsumerId);
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
        String msgID = message.getMsgID();
        log.info("收到消息 id {} body {} tag {}", msgID, msgBody, message.getTag());
        if (publishMessage(message)) {
            return Action.CommitMessage;
        } else {
            log.error("message {} 执行失败,等待重试 重试次数 {}", msgID, message.getReconsumeTimes());
            return Action.ReconsumeLater;
        }
    }

}

