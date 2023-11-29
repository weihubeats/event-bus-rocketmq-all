package com.event.bus.rocketmq.boot.storage;

import com.event.bus.rocketmq.boot.core.EventBusAbstractMessage;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.util.ObjectUtils;

/**
 * @author : wh
 * @date : 2023/11/29 10:20
 * @description:
 */
@RequiredArgsConstructor
public class RedisMethodSuccessStorageImpl extends AbstractMethodSuccessStorage {

    private final RedissonClient redissonClient;

    private final Map<String, Set<String>> methodCache = new ConcurrentHashMap<>();

    @Override
    public Set<String> getSuccessMethod(EventBusAbstractMessage abstractMessage) {
        String msgId = abstractMessage.getMsgId();
        if (!ObjectUtils.isEmpty(methodCache.get(msgId))) {
            return methodCache.get(msgId);
        }
        RMap<String, String> msgConsumeRecords = redissonClient.getMap(msgId);
        Set<String> emptyList = new HashSet<>();

        if (!msgConsumeRecords.isExists() || msgConsumeRecords.isEmpty()) {
            // key不存在或为空，表示该消息未被记录到消费或未被任何订阅者消费过
            methodCache.put(msgId, emptyList);
            return emptyList;
        }
        // 成功方法集合
        Set<String> successMethodSet = msgConsumeRecords.keySet();
        methodCache.put(msgId, successMethodSet);
        return successMethodSet;
    }

    @Override
    public boolean successMethod(String messageId, Method method) {
        RMap<String, String> stringStringRMap = redissonClient.getMap(redisEventId(messageId));
        String methodKey = stringStringRMap.get(getMethodId(method));
        return !ObjectUtils.isEmpty(methodKey);
    }

    @Override
    public void saveSuccessMethod(String messageId, Method method) {
        // todo: storage optimization
        RMap<String, String> stringStringRMap = redissonClient.getMap(redisEventId(messageId));
        stringStringRMap.put(getMethodId(method), "1");
        // todo: Configurable
        stringStringRMap.expireAsync(1, TimeUnit.DAYS);
        Set<String> strings = methodCache.get(messageId);
        if (ObjectUtils.isEmpty(strings)) {
            strings = new HashSet<>();
        }
        String methodId = getMethodId(method);
        strings.add(methodId);
    }


    private String redisEventId(String eventId) {
        return "EVENT_BUS:" + eventId;
    }
}
