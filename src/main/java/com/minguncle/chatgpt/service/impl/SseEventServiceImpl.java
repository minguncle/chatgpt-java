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
     * @param userId
     * @return
     */
    @Override
    public SseEmitter connect(String userId)  {

            SseEmitter sseEmitter = new SseEmitter(0L);
            //sseEmitter.send(SseEmitter.event().data("连接成功"));
            map.put(userId, sseEmitter);
            count.getAndIncrement();
            log.info("创建新的sse连接，当前用户：{}", userId);
            return sseEmitter;

    }

    /**
     * 根据id获取sse实例
     * @param userID
     * @return
     */
    public SseEmitter getSseEmitter(String userID) {
        SseEmitter sseEmitter = map.get(userID);
        if (sseEmitter == null) {
            return connect(userID);
        }
        return sseEmitter;
    }
}
