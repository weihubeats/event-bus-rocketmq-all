package com.event.bus.rocketmq.apache;

import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.EventBusMessageQueue;
import com.event.bus.rocketmq.factory.EventBusMessageQueueSelector;
import com.event.bus.rocketmq.factory.consumer.EventBusConsumeContext;
import com.event.bus.rocketmq.factory.consumer.FEventBusMessageListener;
import com.event.bus.rocketmq.factory.producer.EventBusProducer;
import com.event.bus.rocketmq.factory.producer.EventBusSendResult;
import com.event.bus.rocketmq.factory.status.EventBusAction;
import com.event.bus.rocketmq.factory.status.EventBusSendStatus;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class ApacheMQConverter {

    private static final Field propertiesField;

    static {
        try {
            propertiesField = Message.class.getDeclaredField("properties");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        propertiesField.setAccessible(true);
    }

    @SneakyThrows
    public static Message toRocketMQMessage(EventBusMessage eventBusMessage) {
        Message msg = new Message();
        try {
            propertiesField.set(msg, eventBusMessage.getUserProperties());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        msg.setTopic(eventBusMessage.getTopic());
        msg.setBody(eventBusMessage.getBody());
        msg.setTags(eventBusMessage.getTag());
        if (eventBusMessage.getKey() != null) {
            msg.setKeys(eventBusMessage.getKey());
        }
        Properties systemProperties = eventBusMessage.getSystemProperties();
        if (systemProperties != null) {
            String shardingKey = systemProperties.getProperty(EventBusMessage.SystemPropKey.SHARDINGKEY);
            if (shardingKey != null) {
                Map<String, String> p = (Map<String, String>) propertiesField.get(msg);
                p.put(EventBusMessage.SystemPropKey.SHARDINGKEY, shardingKey);
            }
        }
        msg.setDeliverTimeMs(eventBusMessage.getStartDeliverTime());
        return msg;
    }

    public static EventBusSendResult toEventBusSendResult(SendResult sendResult) {
        EventBusSendResult result = new EventBusSendResult();
        result.setMsgId(sendResult.getMsgId());
        result.setSendStatus(toSendStatus(sendResult.getSendStatus()));
        result.setOffsetMsgId(sendResult.getOffsetMsgId());
        result.setQueueOffset(sendResult.getQueueOffset());
        result.setTransactionId(sendResult.getTransactionId());
        result.setMessageQueue(toEventBusMessageQueue(sendResult.getMessageQueue()));
        return result;
    }

    public static EventBusSendStatus toSendStatus(SendStatus sendStatus) {
        switch (sendStatus) {
            case SEND_OK:
                return EventBusSendStatus.SEND_OK;
            case FLUSH_DISK_TIMEOUT:
                return EventBusSendStatus.FLUSH_DISK_TIMEOUT;
            case FLUSH_SLAVE_TIMEOUT:
                return EventBusSendStatus.FLUSH_SLAVE_TIMEOUT;
            case SLAVE_NOT_AVAILABLE:
                return EventBusSendStatus.SLAVE_NOT_AVAILABLE;
            default:
                throw new RuntimeException();
        }
    }

    private static EventBusMessageQueue toEventBusMessageQueue(MessageQueue messageQueue) {
        EventBusMessageQueue eventBusMessageQueue = new EventBusMessageQueue();
        eventBusMessageQueue.setQueueId(messageQueue.getQueueId());
        eventBusMessageQueue.setTopic(messageQueue.getTopic());
        eventBusMessageQueue.setBrokerName(messageQueue.getBrokerName());
        return eventBusMessageQueue;
    }

    public static EventBusProducer toEventBusProducer(DefaultMQProducer producer) {
        return new ApacheProducer(producer);
    }

    public static MessageListener toRocketMQListener(FEventBusMessageListener listener) {
        // must user new, Because bytecode doesn't work for lambda enhancement
        return new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                List<EventBusMessage> eventBusMessages = msgs.stream()
                    .map(ApacheMQConverter::toEventBusMessage)
                    .collect(Collectors.toList());

                EventBusConsumeContext busConsumeContext = consumeContextFromOpen(context);
                Stream<ConsumeConcurrentlyStatus> statusStream = eventBusMessages.stream()
                    .map(busMessage -> {
                        EventBusAction consume = listener.consume(busMessage, busConsumeContext);
                        return toConsumeConcurrentlyStatus(consume);
                    });
                boolean allSuccess = statusStream.allMatch(s -> s == ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
                // todo producerPreHandle
                if (allSuccess) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } else {
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        };
    }

    @SneakyThrows
    private static EventBusMessage toEventBusMessage(MessageExt message) {
        EventBusMessage result = new EventBusMessage();
        result.setTopic(message.getTopic());
        result.setBody(message.getBody());
//        result.putUserProperties("GREY_TAG", message.getUserProperty("GREY_TAG"));
        result.setTag(message.getTags());
        result.setKey(message.getKeys());
        result.setMsgID(message.getMsgId());
        result.setBornHost(message.getBornHost().toString());
        result.setBornTimestamp(message.getBornTimestamp());
        result.setReconsumeTimes(message.getReconsumeTimes());
        result.setQueueId(message.getQueueId());

        Map<String, String> o = (Map<String, String>) propertiesField.get(message);
        if (o != null) {
            String shardingKey = o.get(EventBusMessage.SystemPropKey.SHARDINGKEY);
            if (shardingKey != null) {
                result.putSystemProperties(EventBusMessage.SystemPropKey.SHARDINGKEY, shardingKey);
            }
        }

        return result;
    }

    private static EventBusMessage toEventBusMessage(Message message) {
        EventBusMessage result = new EventBusMessage();
        result.setTopic(message.getTopic());
        result.setBody(message.getBody());
        result.setTag(message.getTags());
        result.setKey(message.getKeys());
        return result;
    }

    private static EventBusConsumeContext consumeContextFromOpen(ConsumeConcurrentlyContext context) {
        EventBusConsumeContext consumeContext = new EventBusConsumeContext();
        consumeContext.setAcknowledgeIndex(context.getAckIndex());
        return consumeContext;
    }

    public static ConsumeConcurrentlyStatus toConsumeConcurrentlyStatus(EventBusAction action) {
        switch (action) {
            case ReconsumeLater:
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            case CommitMessage:
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            default:
                throw new RuntimeException();
        }
    }

    private static MessageQueue toMessageQueue(EventBusMessageQueue eventBusMessageQueue) {
        MessageQueue messageQueue = new MessageQueue();
        messageQueue.setQueueId(eventBusMessageQueue.getQueueId());
        messageQueue.setTopic(eventBusMessageQueue.getTopic());
        messageQueue.setBrokerName(eventBusMessageQueue.getBrokerName());
        return messageQueue;
    }

    public static MessageQueueSelector toMessageQueueSelector(EventBusMessageQueueSelector selector) {
        return new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                List<EventBusMessageQueue> messageQueues = mqs.stream().map(ApacheMQConverter::toEventBusMessageQueue)
                    .collect(Collectors.toList());
                EventBusMessage eventBusMessage = toEventBusMessage(msg);
                EventBusMessageQueue selected = selector.select(messageQueues, eventBusMessage, arg);
                return toMessageQueue(selected);
            }
        };
    }
}
