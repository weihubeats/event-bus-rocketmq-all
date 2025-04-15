package com.event.bus.rocketmq.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2023/11/24 16:24
 * @description:
 */
@ConfigurationProperties(prefix = "event.bus")
@Data
public class EventBusRocketMQProperties {

    private String aliMQAccessKey;

    private String aliMQSecretKey;

    private String aliYunNameServer;

    private String domain;

    private String subgroup;

    private Producer producer;

    private Consumer consumer;

    private String topic;

    private Integer maxReconsumeTimes = 8;

    /**
     * 是否注册消费者
     */
    private boolean consumerFlag = true;

    /**
     * 是否开启消息轨迹
     */
    private boolean enableMsgTrace = false;

    /**
     * 飞书监控报警webhook 没有则不推送
     */
    private String larkWebHook;

    /**
     * 飞书报警图片
     */
    private String larkImage = "";

    @Data
    public static class Producer {

        private String apacheNameServer;

        private String onsNameServer;

        private String topic;

        private String groupID;

        /**
         * 事件执行时间超时监控
         */
        private Long eventBusTimeOut = 15L;

        /**
         * 消费线程数 默认20
         */
        private Integer consumeThreadNums = 20;

        /**
         * 最大消费重试次数 默认 16次
         */
        private Integer maxReconsumeTimes = 16;

    }

    @Data
    public static final class Consumer {

        private String groupId;

        private String tag = "*";

        private String apacheNameServer;
        
        private String onsNameServer;

        private Integer consumerThreadNums = 20;

        private Integer consumerThreadMaxNums = 20;

        private Integer maxReconsumeTimes = 16;

    }

}
