package com.minguncle.chatgpt.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author wanglinjie
 * @since 2023-03-08 17:09
 */
public interface SseEventService {

    /**
     * 建立Sse连接
     *
     * @param traceId
     * @return
     */
    SseEmitter connect(String traceId) throws Exception;

    /**
     * 根据id获取sse实例
     *
     * @param traceId
     * @return
     */
    SseEmitter getSseEmitter(String traceId);
}
