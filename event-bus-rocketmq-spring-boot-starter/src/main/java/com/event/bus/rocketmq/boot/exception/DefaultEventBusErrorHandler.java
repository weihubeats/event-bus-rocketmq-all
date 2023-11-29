package com.event.bus.rocketmq.boot.exception;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.aliyun.openservices.ons.api.Message;
import com.event.bus.rocketmq.boot.autoconfigure.EventBusRocketMQProperties;
import com.event.bus.rocketmq.boot.core.EventBusErrorHandler;
import com.event.bus.rocketmq.boot.core.EventBusSubscriberExceptionContext;
import com.event.bus.rocketmq.boot.utils.ThreadFactoryImpl;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

/**
 * @author : wh
 * @date : 2023/11/29 10:22
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultEventBusErrorHandler implements EventBusErrorHandler {

    private static final int FEISHU_MESSAGE_HASH_MAX_LENGTH = 30 * 1024;

    private final EventBusRocketMQProperties eventBusRocketMQProperties;

    private static final ExecutorService executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100), new ThreadFactoryImpl("feishu-"));;


    private static final String LARK_NOTICE_JSON_STR = "{\"msg_type\":\"interactive\",\"card\":{\"config\":{\"wide_screen_mode\":true},\"elements\":[{\"tag\":\"img\",\"title\":{\"tag\":\"lark_md\",\"content\":\"\"},\"img_key\":\""+ "%s" + "\",\"alt\":{\"tag\":\"plain_text\",\"content\":\"图片\"}},{\"tag\":\"markdown\",\"content\":\"**msgID:** %s\\n**tag:** %s\\n**method:** %s\\n**重试次数:** %s\\n**exception:** %s\\n\",\"href\":{\"urlVal\":{\"url\":\"https://www.feishu.com\",\"android_url\":\"https://developer.android.com/\",\"ios_url\":\"lark://msgcard/unsupported_action\",\"pc_url\":\"https://www.feishu.com\"}}}],\"header\":{\"template\":\"red\",\"title\":{\"content\":\"event-bus异常报警\",\"tag\":\"plain_text\"}}}}";

    @Override
    public void handleException(Throwable exception, EventBusSubscriberExceptionContext context) {
        String larkWebHook = eventBusRocketMQProperties.getLarkWebHook();
        if (ObjectUtils.isEmpty(larkWebHook)) {
            log.error("consumer fails, but no monitoring link is configured");
            return;
        }
        Message message = context.getMessage();
        String msgID = message.getMsgID();
        String tag = message.getTag();
        String methodName = context.getMethod().getName();
        log.error("消息消费失败, 消息id {} method {}", msgID, methodName, exception);

        int reconsumeTimes = message.getReconsumeTimes();
        // 重试超过10报警
        if (reconsumeTimes >= 8 && reconsumeTimes % 2 == 0) {
            String stackTrace = getStackTrace(exception);
            if (!ObjectUtils.isEmpty(stackTrace)) {
                stackTrace = stackTrace.replaceAll("\n", "\\\\n");
                stackTrace = stackTrace.replaceAll("\t", "\\\\t");
                if (stackTrace.getBytes(StandardCharsets.UTF_8).length > FEISHU_MESSAGE_HASH_MAX_LENGTH) {
                    stackTrace = stackTrace.substring(0, new String(new byte[FEISHU_MESSAGE_HASH_MAX_LENGTH]).length());
                }
            }
            String feiShuText = String.format(LARK_NOTICE_JSON_STR, eventBusRocketMQProperties.getLarkImage(), msgID, tag, methodName, reconsumeTimes, stackTrace);
            executor.execute(() -> {
                try {
                    // todo 暂时不做判断处理
                    HttpResponse execute = HttpRequest.post(larkWebHook).body(feiShuText).execute();
                } catch (Exception e) {
                    log.error("发送飞书异常 ", e);
                }
            });
        }

    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }
}
