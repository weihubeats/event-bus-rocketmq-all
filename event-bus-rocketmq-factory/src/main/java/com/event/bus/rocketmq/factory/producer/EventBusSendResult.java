package com.event.bus.rocketmq.factory.producer;

import com.alibaba.fastjson.JSON;
import com.event.bus.rocketmq.factory.EventBusMessageQueue;
import com.event.bus.rocketmq.factory.status.EventBusSendStatus;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusSendResult {
    
    private EventBusSendStatus sendStatus;
    
    private String msgId;
    
    private EventBusMessageQueue messageQueue;
    
    private long queueOffset;
    
    private String transactionId;
    
    private String offsetMsgId;
    
    private String regionId;
    
    private boolean traceOn = true;

    public EventBusSendResult() {
    }

    public EventBusSendResult(EventBusSendStatus sendStatus, String msgId, String offsetMsgId,
        EventBusMessageQueue messageQueue,
        long queueOffset) {
        this.sendStatus = sendStatus;
        this.msgId = msgId;
        this.offsetMsgId = offsetMsgId;
        this.messageQueue = messageQueue;
        this.queueOffset = queueOffset;
    }

    public EventBusSendResult(final EventBusSendStatus sendStatus, final String msgId,
        final EventBusMessageQueue messageQueue,
        final long queueOffset, final String transactionId,
        final String offsetMsgId, final String regionId) {
        this.sendStatus = sendStatus;
        this.msgId = msgId;
        this.messageQueue = messageQueue;
        this.queueOffset = queueOffset;
        this.transactionId = transactionId;
        this.offsetMsgId = offsetMsgId;
        this.regionId = regionId;
    }

    public static String encoderSendResultToJson(final Object obj) {
        return JSON.toJSONString(obj);
    }

    public static EventBusMessageQueue decoderSendResultFromJson(String json) {
        return JSON.parseObject(json, EventBusMessageQueue.class);
    }

    public boolean isTraceOn() {
        return traceOn;
    }

    public void setTraceOn(final boolean traceOn) {
        this.traceOn = traceOn;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(final String regionId) {
        this.regionId = regionId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public EventBusSendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(EventBusSendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public EventBusMessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(EventBusMessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOffsetMsgId() {
        return offsetMsgId;
    }

    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    @Override
    public String toString() {
        return "SendResult [sendStatus=" + sendStatus + ", msgId=" + msgId + ", offsetMsgId=" + offsetMsgId + ", messageQueue=" + messageQueue
            + ", queueOffset=" + queueOffset + "]";
    }
}
