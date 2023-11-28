package com.event.bus.rocketmq.boot.storage;

import com.event.bus.rocketmq.boot.core.EventBusAbstractMessage;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author : wh
 * @date : 2023/11/27 18:08
 * @description:
 */
public interface MethodSuccessStorage {

    /**
     * 获取方法执行成功的方法
     *
     * @return
     */

    Set<String> getSuccessMethod(EventBusAbstractMessage abstractMessage);

    /**
     * 是否执行成功
     * @param method
     * @return
     */
    boolean successMethod(String messageId, Method method);


    /**
     * 保存执行成功方法
     */
    void saveSuccessMethod(String messageId, Method method);


}
