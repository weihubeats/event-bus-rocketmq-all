package com.event.bus.rocketmq.boot.storage;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.springframework.util.DigestUtils;

/**
 * @author : wh
 * @date : 2023/11/29 10:20
 * @description:
 */
public abstract class AbstractMethodSuccessStorage implements MethodSuccessStorage {

    /**
     * get md5 method id
     *
     * @param method
     * @return
     */
    public String getMethodId(Method method) {
        return DigestUtils.md5DigestAsHex(method.toString().getBytes(StandardCharsets.UTF_8));
    }

}

