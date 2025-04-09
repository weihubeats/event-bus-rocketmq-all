package com.event.bus.rocketmq.apache;

import com.event.bus.rocketmq.factory.EventBusMQException;
import com.event.bus.rocketmq.factory.EventBusMessage;
import com.event.bus.rocketmq.factory.EventBusMessageQueueSelector;
import com.event.bus.rocketmq.factory.producer.EventBusProducer;
import com.event.bus.rocketmq.factory.producer.EventBusSendCallback;
import com.event.bus.rocketmq.factory.producer.EventBusSendResult;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
@RequiredArgsConstructor
public class ApacheProducer implements EventBusProducer {

    private final DefaultMQProducer producer;

    @Override
    public EventBusSendResult send(EventBusMessage msg, EventBusMessageQueueSelector selector, Object key) {
        
        Message message = ApacheMQConverter.toRocketMQMessage(msg);
        MessageQueueSelector messageQueueSelector = ApacheMQConverter.toMessageQueueSelector(selector);
        SendResult send;
        try {
            send = producer.send(message, messageQueueSelector, key);
        }
        catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            throw new EventBusMQException(e);
        }

        // todo producerPreHandle
        return ApacheMQConverter.toEventBusSendResult(send);
    }

    @Override
    public void send(EventBusMessage msg, EventBusSendCallback sendCallback) throws EventBusMQException {
        Message message = ApacheMQConverter.toRocketMQMessage(msg);
        try {
            SendResult send = producer.send(message);
            sendCallback.onSuccess(ApacheMQConverter.toEventBusSendResult(send));
        } catch (Exception e) {
            sendCallback.onException(e);
        }
        // todo producerPreHandle

    }

    @Override
    public void sendOneway(EventBusMessage message) {
        try {
            Message msg = ApacheMQConverter.toRocketMQMessage(message);
            producer.sendOneway(msg);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            throw new EventBusMQException(e);
        }
        // todo producerPreHandle

    }

    @Override
    public EventBusSendResult send(EventBusMessage message) {
        Message msg = ApacheMQConverter.toRocketMQMessage(message);
        SendResult send;
        try {
            send = producer.send(msg);
        } catch (MQClientException | MQBrokerException | InterruptedException | RemotingException e) {
            throw new EventBusMQException(e);
        }
        message.setMsgID(send.getMsgId());
        // todo producerPreHandle
        return ApacheMQConverter.toEventBusSendResult(send);
    }

    @Override
    public void start() {
        try {
            wrapProducerGroup(producer);
            producer.start();
        } catch (MQClientException e) {
            throw new EventBusMQException(e);
        }

    }

    private void wrapProducerGroup(DefaultMQProducer producer) {
        String producerGroup = producer.getProducerGroup();
        if (producerGroup == null || producerGroup.equals("DEFAULT_PRODUCER")) {
            producer.setProducerGroup("EVENT_BUS_PRODUCER");
        }
    }

    @Override
    public void shutdown() {
        this.producer.shutdown();
    }
}
