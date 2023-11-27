package com.event.bus.rocketmq.boot.core;

import lombok.Data;

/**
 * @author : wh
 * @date : 2023/11/24 16:29
 * @description:
 */
@Data
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

}
