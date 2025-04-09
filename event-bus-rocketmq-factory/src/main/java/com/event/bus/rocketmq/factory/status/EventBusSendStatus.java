package com.event.bus.rocketmq.factory.status;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public enum EventBusSendStatus {

    SEND_OK,
    FLUSH_DISK_TIMEOUT,
    FLUSH_SLAVE_TIMEOUT,
    SLAVE_NOT_AVAILABLE,

}
