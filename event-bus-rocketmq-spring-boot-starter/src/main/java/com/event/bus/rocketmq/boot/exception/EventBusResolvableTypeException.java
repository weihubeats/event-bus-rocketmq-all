package com.event.bus.rocketmq.boot.exception;

/**
 * @author : wh
 * @date : 2023/11/27 18:16
 * @description:
 */
public class EventBusResolvableTypeException extends RuntimeException{

    public EventBusResolvableTypeException(String message) {
        super(message);
    }
}
