package com.event.bus.rocketmq.factory.producer;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public interface EventBusSendCallback {

    void onSuccess(EventBusSendResult var1);

    void onException(Throwable var1);

}
