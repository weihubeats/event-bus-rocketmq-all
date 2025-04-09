package com.event.bus.rocketmq.factory;

import java.util.List;

/**
 * @author : wh
 * @date : 2025/4/8
 * @description:
 */
public interface EventBusMessageQueueSelector {

    EventBusMessageQueue select(final List<EventBusMessageQueue> mqs, final EventBusMessage msg, final Object arg);

}
