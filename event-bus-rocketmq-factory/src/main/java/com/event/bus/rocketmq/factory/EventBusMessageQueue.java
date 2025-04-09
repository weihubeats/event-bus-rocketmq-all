package com.event.bus.rocketmq.factory;

import java.io.Serializable;

/**
 * @author : wh
 * @date : 2025/4/7
 * @description:
 */
public class EventBusMessageQueue implements Comparable<EventBusMessageQueue>, Serializable {

    private static final long serialVersionUID = 6191200464116433425L;

    public static final int NO_QUEUE_GROUP = -1;

    public static final int PLACEHOLDER_QUEUE_ID = -1;

    private String topic;

    private String brokerName;

    private int queueId;

    private int queueGroupId = NO_QUEUE_GROUP;

    private boolean isMainQueue;

    public EventBusMessageQueue() {

    }

    public EventBusMessageQueue(String topic, String brokerName, int queueId) {
        this.topic = topic;
        this.brokerName = brokerName;
        this.queueId = queueId;
    }

    public EventBusMessageQueue(String topic, String brokerName, int queueId, int queueGroupId, boolean mainQueue) {
        this(topic, brokerName, queueId);
        this.queueGroupId = queueGroupId;
        this.isMainQueue = mainQueue;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueGroupId() {
        return queueGroupId;
    }

    public void setQueueGroupId(int queueGroupId) {
        this.queueGroupId = queueGroupId;
    }

    public String generateKey() {
        return brokerName + "-" + queueId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
        result = prime * result + queueId;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventBusMessageQueue other = (EventBusMessageQueue) obj;
        if (brokerName == null) {
            if (other.brokerName != null) {
                return false;
            }
        } else if (!brokerName.equals(other.brokerName)) {
            return false;
        }
        if (queueId != other.queueId) {
            return false;
        }
        if (topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!topic.equals(other.topic)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MessageQueue [topic=" + topic + ", brokerName=" + brokerName + ", queueId=" + queueId + "]";
    }

    @Override
    public int compareTo(EventBusMessageQueue o) {
        {
            int result = this.topic.compareTo(o.topic);
            if (result != 0) {
                return result;
            }
        }

        {
            int result = this.brokerName.compareTo(o.brokerName);
            if (result != 0) {
                return result;
            }
        }

        return this.queueId - o.queueId;
    }

    public boolean isMainQueue() {
        return isMainQueue;
    }

    public void setMainQueue(boolean mainQueue) {
        isMainQueue = mainQueue;
    }
}
