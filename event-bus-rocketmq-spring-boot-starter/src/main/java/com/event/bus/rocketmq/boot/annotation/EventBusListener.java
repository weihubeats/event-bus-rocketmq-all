package com.event.bus.rocketmq.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * @author : wh
 * @date : 2023/11/24 16:21
 * @description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventBusListener {

    @AliasFor("classes")
    Class<?>[] value() default {};

    /**
     * 方法最长执行时间,单位 秒
     */
    int maxExecuteTime() default 10;

    @AliasFor("value")
    Class<?>[] classes() default {};

    String id() default "";

    String tag() default "";
}
