package com.event.bus.rocketmq.boot.core;

import com.aliyun.openservices.ons.api.Consumer;
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

    private final Consumer consumer;


    @Override
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            this.consumer.shutdown();
        }

    }
}
