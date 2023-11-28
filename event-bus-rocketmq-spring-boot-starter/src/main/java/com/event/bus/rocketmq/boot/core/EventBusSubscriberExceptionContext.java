package com.event.bus.rocketmq.boot.core;

import com.aliyun.openservices.ons.api.Message;
import java.lang.reflect.Method;
import lombok.Data;

/**
 * @author : wh
 * @date : 2023/11/27 18:07
 * @description:
 */
@Data
public class EventBusSubscriberExceptionContext {


    private Message message;

    private Method method;

}
