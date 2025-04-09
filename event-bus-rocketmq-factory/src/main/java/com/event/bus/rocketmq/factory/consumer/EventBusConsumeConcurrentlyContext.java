package com.event.bus.rocketmq.factory.consumer;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusConsumeConcurrentlyContext {
    /**
     * Message consume retry strategy<br>
     * -1,no retry,put into DLQ directly<br>
     * 0,broker control retry frequency<br>
     * >0,client control retry frequency
     */
    protected int delayLevelWhenNextConsume = 0;
    
    protected int ackIndex = Integer.MAX_VALUE;


    public EventBusConsumeConcurrentlyContext() {
    }

    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }


    public int getAckIndex() {
        return ackIndex;
    }

    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }


}
