package com.minguncle.chatgpt.service;

import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface OpenAIChatGPTService {
    /**
     * 返回处理过的字符串
     *
     * @param request
     * @return
     */
    String chat(ChatRequest request);

    /**
     * 流式返回处理过的字符串
     *
     * @param request
     * @return
     */
    SseEmitter streamChat(ChatRequest request);

    /**
     * ChatGPT Proxy
     * @param request
     * @return
     */
    Object completions(ChatRequest request);
}
