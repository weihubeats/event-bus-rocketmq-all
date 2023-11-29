package com.event.bus.rocketmq.boot.core;

import lombok.Data;

/**
 * @author : wh
 * @date : 2023/11/24 16:29
 * @description:
 */
public abstract class EventBusAbstractMessage {

    private static final Integer DEFAULT_VERSION = 1;

    private String msgId;

    /**
     * 业务唯一id, 可用于做幂等
     */
    private String key;

    /**
     * 路由key, 用于做分区有序消息, 不用可以不提供
     */
    private String shardingKey;

    /**
     * 消息tag
     */
    public abstract String getTag();


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }


    public Integer getVersion() {
        return DEFAULT_VERSION;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    
    

}
