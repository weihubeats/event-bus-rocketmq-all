package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.factory.EventBusMessage;
import java.lang.reflect.Method;
import lombok.Data;

/**
 * @author : wh
 * @date : 2023/11/27 18:07
 * @description:
 */
@Data
public class EventBusSubscriberExceptionContext {


    private EventBusMessage message;

    private Method method;

}
