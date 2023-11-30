package com.event.bus.rocketmq.spring.boot.sample.event;

import com.event.bus.rocketmq.boot.core.EventBusAbstractMessage;
import lombok.Data;

/**
 * @author : wh
 * @date : 2023/11/30 09:48
 * @description:
 */
@Data
public class OrderEvent extends EventBusAbstractMessage {

    private String msg;


    public static final String TAG = "ORDER_MESSAGE";
    
    @Override
    public String getTag() {
        return TAG;
    }
}
