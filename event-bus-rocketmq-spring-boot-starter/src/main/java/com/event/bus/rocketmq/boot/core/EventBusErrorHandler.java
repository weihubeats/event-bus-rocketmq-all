package com.event.bus.rocketmq.boot.core;

/**
 * @author : wh
 * @date : 2023/11/27 18:06
 * @description:
 */
public interface EventBusErrorHandler {

    void handleException(Throwable exception, EventBusSubscriberExceptionContext context);

}
