package com.event.bus.rocketmq.factory;

import java.util.Properties;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusMessage {

    private String topic;

    private byte[] body;

    private Properties systemProperties;

    private Properties userProperties;

    private int queueId;

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public EventBusMessage() {
    }

    public EventBusMessage(String topic, String tag, byte[] body) {
        this.topic = topic;
        this.body = body;

        putSystemProperties(SystemPropKey.TAG, tag);
    }

    public EventBusMessage(String topic, String tag, String key, byte[] body) {
        this.topic = topic;
        this.body = body;

        this.putSystemProperties(SystemPropKey.TAG, tag);
        this.putSystemProperties(SystemPropKey.KEY, key);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void putSystemProperties(final String key, final String value) {
        if (null == this.systemProperties) {
            this.systemProperties = new Properties();
        }

        if (key != null && value != null) {
            this.systemProperties.put(key, value);
        }
    }

    public void putUserProperties(final String key, final String value) {
        if (null == this.userProperties) {
            this.userProperties = new Properties();
        }

        if (key != null && value != null) {
            this.userProperties.put(key, value);
        }
    }

    public void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public Properties getUserProperties() {
        return userProperties;
    }

    public String getUserProperties(final String key) {
        if (null != this.userProperties) {
            return (String) this.userProperties.get(key);
        }

        return null;
    }

/*    public TopicPartition getTopicPartition() {
        return new TopicPartition(topic, getSystemProperties(SystemPropKey.PARTITION));
    }*/

    public void setUserProperties(Properties userProperties) {
        this.userProperties = userProperties;
    }

    public String getTopic() {
        return topic;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getTag() {
        return getSystemProperties(SystemPropKey.TAG);
    }

    public void setTag(String tag) {
        putSystemProperties(SystemPropKey.TAG, tag);
    }

    String getSystemProperties(final String key) {
        if (null != this.systemProperties) {
            return this.systemProperties.getProperty(key);
        }

        return null;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public int getReconsumeTimes() {
        String pro = this.getSystemProperties(SystemPropKey.RECONSUMETIMES);
        if (pro != null) {
            return Integer.parseInt(pro);
        }

        return 0;
    }

    public void setReconsumeTimes(final int value) {
        putSystemProperties(SystemPropKey.RECONSUMETIMES, String.valueOf(value));
    }

    public String getMsgID() {
        return this.getSystemProperties(SystemPropKey.MSGID);
    }

    public void setStartDeliverTime(final long value) {
        putSystemProperties(SystemPropKey.STARTDELIVERTIME, String.valueOf(value));
    }

    public long getStartDeliverTime() {
        String pro = this.getSystemProperties(SystemPropKey.STARTDELIVERTIME);
        if (pro != null) {
            return Long.parseLong(pro);
        }

        return 0L;
    }

    public String getKey() {
        return this.getSystemProperties(SystemPropKey.KEY);
    }

    /**
     * 设置业务码
     *
     * @param key 业务码
     */
    public void setKey(String key) {
        this.putSystemProperties(SystemPropKey.KEY, key);
    }

    public void setMsgID(String msgid) {
        this.putSystemProperties(SystemPropKey.MSGID, msgid);
    }

    public String getShardingKey() {
        String pro = this.getSystemProperties(SystemPropKey.SHARDINGKEY);
        return pro == null ? "" : pro;
    }

    public void setShardingKey(final String value) {
        putSystemProperties(SystemPropKey.SHARDINGKEY, value);
    }

    public long getBornTimestamp() {
        String pro = this.getSystemProperties(SystemPropKey.BORNTIMESTAMP);
        if (pro != null) {
            return Long.parseLong(pro);
        }

        return 0L;
    }

    /**
     * 设置消息的产生时间.
     *
     * @param value 消息生产时间.
     */
    public void setBornTimestamp(final long value) {
        putSystemProperties(SystemPropKey.BORNTIMESTAMP, String.valueOf(value));
    }

    /**
     * 获取产生消息的主机.
     *
     * @return 产生消息的主机
     */
    public String getBornHost() {
        String pro = this.getSystemProperties(SystemPropKey.BORNHOST);
        return pro == null ? "" : pro;
    }

    /**
     * 设置生产消息的主机
     *
     * @param value 生产消息的主机
     */
    public void setBornHost(final String value) {
        putSystemProperties(SystemPropKey.BORNHOST, value);
    }

    static public class SystemPropKey {
        public static final String TAG = "__TAG";
        public static final String KEY = "__KEY";
        public static final String MSGID = "__MSGID";
        public static final String SHARDINGKEY = "__SHARDINGKEY";
        public static final String RECONSUMETIMES = "__RECONSUMETIMES";
        public static final String BORNTIMESTAMP = "__BORNTIMESTAMP";
        public static final String BORNHOST = "__BORNHOST";
        /**
         * 设置消息的定时投递时间（绝对时间). <p>例1: 延迟投递, 延迟3s投递, 设置为: System.currentTimeMillis() + 3000; <p>例2: 定时投递, 2016-02-01
         * 11:30:00投递, 设置为: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-01 11:30:00").getTime()
         */
        public static final String STARTDELIVERTIME = "__STARTDELIVERTIME";

        public static final String CONSUMEOFFSET = "__CONSUMEOFFSET";

        public static final String PARTITION = "__PARTITION";
    }
}
