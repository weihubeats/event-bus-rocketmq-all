package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.factory.consumer.EventBusConsumer;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author : wh
 * @date : 2023/11/27 18:26
 * @description:
 */
@RequiredArgsConstructor
public class EventBusConsumerHolder implements DisposableBean {

    private final EventBusConsumer consumer;

    @Override
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            this.consumer.shutdown();
        }

    }
}
