package com.event.bus.rocketmq.boot.core;

import com.event.bus.rocketmq.boot.annotation.EventBusListener;
import com.event.bus.rocketmq.boot.storage.MethodSuccessStorage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author : wh
 * @date : 2023/11/27 18:12
 * @description:
 */
@Slf4j
public class EventBusApplicationListenerMethodAdapter implements EventBusGenericApplicationListener{

    private final String beanName;

    private final Method method;

    private final Method targetMethod;

    private final int order;

    private final AnnotatedElementKey methodKey;

    /**
     * 方法tag
     */
    private final String tag;

    private final List<ResolvableType> declaredEventTypes;

    @Nullable
    private volatile String listenerId;

    @Nullable
    private final ApplicationContext applicationContext;


    public EventBusApplicationListenerMethodAdapter(String beanName, String tag, Class<?> targetClass, Method method, ApplicationContext applicationContext) {
        this.beanName = beanName;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
            AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
        this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
        this.tag = tag;
        EventBusListener ann = AnnotatedElementUtils.findMergedAnnotation(this.targetMethod, EventBusListener.class);
        this.declaredEventTypes = resolveDeclaredEventTypes(method, ann);
        this.order = resolveOrder(this.targetMethod);
        String id = (ann != null ? ann.id() : "");
        this.listenerId = (!id.isEmpty() ? id : null);
        this.applicationContext = applicationContext;
    }

    private static List<ResolvableType> resolveDeclaredEventTypes(Method method, @Nullable EventBusListener ann) {
        int count = method.getParameterCount();
        if (count > 1) {
            throw new IllegalStateException(
                "Maximum one parameter is allowed for event listener method: " + method);
        }

        if (ann != null) {
            Class<?>[] classes = ann.classes();
            if (classes.length > 0) {
                List<ResolvableType> types = new ArrayList<>(classes.length);
                for (Class<?> eventType : classes) {
                    types.add(ResolvableType.forClass(eventType));
                }
                return types;
            }
        }

        if (count == 0) {
            throw new IllegalStateException(
                "Event parameter is mandatory for event listener method: " + method);
        }
        return Collections.singletonList(ResolvableType.forMethodParameter(method, 0));
    }

    private static int resolveOrder(Method method) {
        Order ann = AnnotatedElementUtils.findMergedAnnotation(method, Order.class);
        return (ann != null ? ann.value() : Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        for (ResolvableType declaredEventType : this.declaredEventTypes) {
            if (declaredEventType.isAssignableFrom(eventType)) {
                return true;
            }
            if (PayloadApplicationEvent.class.isAssignableFrom(eventType.toClass())) {
                ResolvableType payloadType = eventType.as(PayloadApplicationEvent.class).getGeneric();
                if (declaredEventType.isAssignableFrom(payloadType)) {
                    return true;
                }
            }
        }
        return eventType.hasUnresolvableGenerics();
    }

    @Override
    public boolean supportsEventType(String tag) {
        return Objects.equals(tag, this.tag);
    }

    @Override
    public void onApplicationEvent(EventBusAbstractMessage eventBusAbstractMessage) {
        // todo: validation args
        Object[] args = new Object[] {eventBusAbstractMessage};
        doInvoke(args);
        // 保存执行成功的方法
        MethodSuccessStorage bean = applicationContext.getBean(MethodSuccessStorage.class);
        bean.saveSuccessMethod(eventBusAbstractMessage.getMsgId(), method);
    }

    protected Object doInvoke(Object... args) {
        Object bean = getTargetBean();
        // Detect package-protected NullBean instance through equals(null) check
        if (bean.equals(null)) {
            return null;
        }

        ReflectionUtils.makeAccessible(this.method);
        try {
            return this.method.invoke(bean, args);
        }
        catch (IllegalArgumentException ex) {
            assertTargetBean(this.method, bean, args);
            throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
        }
        catch (IllegalAccessException ex) {
            throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
        }
        catch (InvocationTargetException ex) {
            // Throw underlying exception
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            else {
                String msg = getInvocationErrorMessage(bean, "Failed to invoke event listener method", args);
                throw new UndeclaredThrowableException(targetException, msg);
            }
        }
    }

    protected Object getTargetBean() {
        Assert.notNull(this.applicationContext, "ApplicationContext must no be null");
        return this.applicationContext.getBean(this.beanName);
    }

    public List<ResolvableType> getDeclaredEventTypes() {
        return this.declaredEventTypes;
    }

    private void assertTargetBean(Method method, Object targetBean, Object[] args) {
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        Class<?> targetBeanClass = targetBean.getClass();
        if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
            String msg = "The event listener method class '" + methodDeclaringClass.getName() +
                "' is not an instance of the actual bean class '" +
                targetBeanClass.getName() + "'. If the bean requires proxying " +
                "(e.g. due to @Transactional), please use class-based proxying.";
            throw new IllegalStateException(getInvocationErrorMessage(targetBean, msg, args));
        }
    }

    private String getInvocationErrorMessage(Object bean, String message, Object[] resolvedArgs) {
        StringBuilder sb = new StringBuilder(getDetailedErrorMessage(bean, message));
        sb.append("Resolved arguments: \n");
        for (int i = 0; i < resolvedArgs.length; i++) {
            sb.append('[').append(i).append("] ");
            if (resolvedArgs[i] == null) {
                sb.append("[null] \n");
            }
            else {
                sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
                sb.append("[value=").append(resolvedArgs[i]).append("]\n");
            }
        }
        return sb.toString();
    }

    protected String getDetailedErrorMessage(Object bean, String message) {
        StringBuilder sb = new StringBuilder(message).append('\n');
        sb.append("HandlerMethod details: \n");
        sb.append("Bean [").append(bean.getClass().getName()).append("]\n");
        sb.append("Method [").append(this.method.toGenericString()).append("]\n");
        return sb.toString();
    }

    public Method getMethod() {
        return method;
    }
}
