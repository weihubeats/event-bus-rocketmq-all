package com.event.bus.rocketmq.factory.consumer;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusConsumeContext {

    private int acknowledgeIndex;

    public EventBusConsumeContext() {
    }

    public int getAcknowledgeIndex() {
        return acknowledgeIndex;
    }

    public void setAcknowledgeIndex(int acknowledgeIndex) {
        this.acknowledgeIndex = acknowledgeIndex;
    }
    
}
