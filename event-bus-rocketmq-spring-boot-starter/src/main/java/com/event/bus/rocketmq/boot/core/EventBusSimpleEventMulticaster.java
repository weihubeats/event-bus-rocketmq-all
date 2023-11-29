package com.event.bus.rocketmq.boot.core;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.shade.com.google.common.collect.Lists;
import com.event.bus.rocketmq.boot.constants.EventBusMessageConstants;
import com.event.bus.rocketmq.boot.exception.EventBusResolvableTypeException;
import com.event.bus.rocketmq.boot.storage.MethodSuccessStorage;
import com.event.bus.rocketmq.boot.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

/**
 * @author : wh
 * @date : 2023/11/27 18:04
 * @description:
 */
@Configuration
@ConditionalOnMissingBean(EventBusMessageMulticaster.class)
@Slf4j
public class EventBusSimpleEventMulticaster implements EventBusMessageMulticaster {

    public final Map<String, Set<EventBusMessageListener<?>>> messageListeners = new HashMap<>();

    private final EventBusErrorHandler errorHandler;

    private final MethodSuccessStorage methodSuccessStorage;

    public EventBusSimpleEventMulticaster(@Autowired(required = false) EventBusErrorHandler errorHandler,
        MethodSuccessStorage methodSuccessStorage) {
        this.errorHandler = errorHandler;
        this.methodSuccessStorage = methodSuccessStorage;
    }

    // todo: Configurable
//    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public boolean multicastMessage(Message message, String uniqueConsumerId) {
        boolean success = true;
        String tag = message.getTag();
        Collection<EventBusMessageListener<?>> messageListenersByTag = getMessageListenersByTag(uniqueConsumerId, tag);
        log.info("tag {}, EventBusMessageListener size {}", tag, messageListenersByTag.size());
        Collection<EventBusMessageListener<?>> failOrFirstMessageListener = filterSuccessMethod(messageListenersByTag, message);
        log.info("failOrFirstMessageListener size {}", failOrFirstMessageListener.size());
        for (EventBusMessageListener<?> messageListener : failOrFirstMessageListener) {
            // todo: Supports multi-threaded execution ?
            success = success && invokeListener(messageListener, message);
        }
        // All methods succeed
        return success;

    }

    private Collection<EventBusMessageListener<?>> filterSuccessMethod(
        Collection<EventBusMessageListener<?>> messageListeners, Message message) {
        Collection<EventBusMessageListener<?>> failOrFirstMessageListener = new ArrayList<>();
        messageListeners.forEach(s -> {
            EventBusApplicationListenerMethodAdapter applicationListenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) s;
            if (methodSuccessStorage.successMethod(message.getMsgID(), applicationListenerMethodAdapter.getMethod())) {
                return;
            }
            failOrFirstMessageListener.add(s);
        });
        return failOrFirstMessageListener;
    }

    @Override
    public void addMessageListeners(Collection<EventBusMessageListener<?>> listeners,
        String groupTopicConsumerId) {
        if (Objects.isNull(listeners) || listeners.isEmpty()) {
            return;
        }

        if (this.messageListeners.containsKey(groupTopicConsumerId)) {
            this.messageListeners.get(groupTopicConsumerId).addAll(listeners);
        } else {
            HashSet<EventBusMessageListener<?>> set = new HashSet<>(listeners);
            this.messageListeners.put(groupTopicConsumerId, set);
        }
    }

    private Collection<EventBusMessageListener<?>> getMessageListenersByTag(String consumerId, String tag) {
        // todo: add cache
        ArrayList<EventBusMessageListener<?>> result = Lists.newArrayList();

        if (!this.messageListeners.containsKey(consumerId)) {
            // the consumer has no listeners because the listeners map has no its key
            return result;
        }

        // get the listeners for the consumer
        Set<EventBusMessageListener<?>> listenersForCid = this.messageListeners.get(consumerId);

        for (EventBusMessageListener<?> listener : listenersForCid) {
            if (supportsMessageListener(listener, tag)) {
                result.add(listener);
            }
        }
        return result;
    }

    private boolean supportsMessageListener(EventBusMessageListener<?> listener, ResolvableType eventType) {
        if (listener instanceof EventBusApplicationListenerMethodAdapter) {
            EventBusApplicationListenerMethodAdapter listenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) listener;
            return listenerMethodAdapter.supportsEventType(eventType);
        } else {
            throw new RuntimeException("EventBusMessageListener not instanceof EventBusApplicationListenerMethodAdapter");
        }
    }

    private boolean supportsMessageListener(EventBusMessageListener<?> listener, String tag) {
        if (listener instanceof EventBusApplicationListenerMethodAdapter) {
            EventBusApplicationListenerMethodAdapter listenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) listener;
            return listenerMethodAdapter.supportsEventType(tag);
        } else {
            throw new RuntimeException("EventBusMessageListener not instanceof EventBusApplicationListenerMethodAdapter");
        }
    }

    private ResolvableType resolveDefaultEventType(EventBusAbstractMessage eventBusAbstractMessage) {
        return ResolvableType.forInstance(eventBusAbstractMessage);
    }

    protected boolean invokeListener(EventBusMessageListener<?> listener, Message message) {
        EventBusErrorHandler errorHandler = getErrorHandler();
        if (errorHandler != null) {
            try {
                doInvokeListener(listener, message);
            } catch (Throwable err) {
                errorHandler.handleException(err, builderSubscriberExceptionContext(message, listener));
                return false;
            }
        } else {
            doInvokeListener(listener, message);
        }
        return true;
    }

    public EventBusSubscriberExceptionContext builderSubscriberExceptionContext(Message message,
        EventBusMessageListener<?> listener) {
        EventBusSubscriberExceptionContext subscriberExceptionContext = new EventBusSubscriberExceptionContext();
        subscriberExceptionContext.setMessage(message);
        if (listener instanceof EventBusApplicationListenerMethodAdapter) {
            EventBusApplicationListenerMethodAdapter eventBusApplicationListenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) listener;
            subscriberExceptionContext.setMethod(eventBusApplicationListenerMethodAdapter.getMethod());
        }
        return subscriberExceptionContext;
    }

    private void doInvokeListener(EventBusMessageListener<?> listener, Message message) {
        EventBusApplicationListenerMethodAdapter eventBusApplicationListenerMethodAdapter = (EventBusApplicationListenerMethodAdapter) listener;
        long start = System.currentTimeMillis();
        log.info("event bus mq 消费开始 msgID {} listener {}, method {}", message.getMsgID(), listener.getClass(), eventBusApplicationListenerMethodAdapter.getMethod().getName());
        ResolvableType declaredEventTypes = eventBusApplicationListenerMethodAdapter.getDeclaredEventTypes().stream().findFirst().orElseThrow(() -> new EventBusResolvableTypeException("泛型解析异常"));
        String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
        JsonNode jsonNode = JsonUtil.json2JsonNode(msgBody);
        String data = jsonNode.get(EventBusMessageConstants.EVENT_BUS_DATA).toPrettyString();
        EventBusAbstractMessage abstractMessage = (EventBusAbstractMessage) JsonUtil.json2JavaBean(data, declaredEventTypes.resolve());
        abstractMessage.setMsgId(message.getMsgID());
        eventBusApplicationListenerMethodAdapter.onApplicationEvent(abstractMessage);
        double executionTime = (System.currentTimeMillis() - start) / 1000d;
        log.info("event bus 消费完成 msgID {} 耗时 {}s", abstractMessage.getMsgId(), executionTime);
    }

    protected EventBusErrorHandler getErrorHandler() {
        return this.errorHandler;
    }
}
