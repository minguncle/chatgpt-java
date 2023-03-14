package com.minguncle.chatgpt.service.impl;

import com.minguncle.chatgpt.service.SseEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * sse服务实现类
 *
 * @author wanglinjie
 * @since 2023-03-08 17:09
 */
@Slf4j
@Service
public class SseEventServiceImpl implements SseEventService {

    private static AtomicLong count = new AtomicLong(0);
    private static Map<String, SseEmitter> map = new ConcurrentHashMap<>();

    /**
     * 建立Sse连接
     *
     * @param traceId
     * @return
     */
    @Override
    public SseEmitter connect(String traceId) {

        SseEmitter sseEmitter = new SseEmitter(0L);
        //sseEmitter.send(SseEmitter.event().data("连接成功"));
        map.put(traceId, sseEmitter);
        count.getAndIncrement();
        log.debug("创建新的sse连接，当前用户：[{}],存活sse数: [{}]", traceId, count.get());
        return sseEmitter;

    }

    /**
     * 根据id获取sse实例
     *
     * @param traceId
     * @return
     */
    public SseEmitter getSseEmitter(String traceId) {
        SseEmitter sseEmitter = map.get(traceId);
        if (sseEmitter == null) {
            return connect(traceId);
        }
        return sseEmitter;
    }

    /**
     * 移除sse实例
     *
     * @param traceId
     */
    public static void remove(String traceId) {
        map.remove(traceId);
    }

    /**
     * 关闭并移除sse实例
     *
     * @param traceId
     */
    public static void closeAndRemove(String traceId) {
        SseEmitter sseEmitter = map.get(traceId);
        if (sseEmitter != null) {
            sseEmitter.complete();
            map.remove(traceId);
            log.info("移除sse连接完成,存活连接数:[{}]", count.decrementAndGet());
        }
    }
}
