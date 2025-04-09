package com.event.bus.rocketmq.factory.consumer;

import com.event.bus.rocketmq.factory.status.EventBusExpressionType;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusMessageSelector {

    private EventBusExpressionType type;

    private String subExpression;

    public static EventBusMessageSelector bySql(String subExpression) {
        return new EventBusMessageSelector(EventBusExpressionType.SQL92, subExpression);
    }

    public static EventBusMessageSelector byTag(String subExpression) {
        return new EventBusMessageSelector(EventBusExpressionType.TAG, subExpression);
    }

    private EventBusMessageSelector() {
    }

    private EventBusMessageSelector(EventBusExpressionType type, String subExpression) {
        this.type = type;
        this.subExpression = subExpression;
    }

    public EventBusExpressionType getType() {
        return type;
    }

    public String getSubExpression() {
        return subExpression;
    }
}
