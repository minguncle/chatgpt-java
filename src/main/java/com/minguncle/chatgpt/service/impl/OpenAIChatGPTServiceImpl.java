package com.minguncle.chatgpt.service.impl;

import com.minguncle.chatgpt.config.Config;
import com.minguncle.chatgpt.event.ChatGPTEventListener;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import com.minguncle.chatgpt.service.OpenAIChatGPTService;
import com.minguncle.chatgpt.utils.api.OpenAIChatGPTApiClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Service
public class OpenAIChatGPTServiceImpl implements OpenAIChatGPTService {

    private final OpenAIChatGPTApiClient apiClient;

    @Autowired
    public OpenAIChatGPTServiceImpl(Config config) {
        this.apiClient = new OpenAIChatGPTApiClient(config.getApiKey());
    }

    @Override
    @SneakyThrows
    public String chat(ChatRequest request) {
        return apiClient.chat(request);
    }

    @Override
    @SneakyThrows
    public SseEmitter streamChat(ChatRequest request) {
        request.setStream(true);
        SseEmitter sseEmitter = new SseEmitter(5000L);
        String traceId = UUID.randomUUID().toString();
        sseEmitter.send(SseEmitter.event().id(traceId).name("update"));
        ChatGPTEventListener chatGPTEventListener = new ChatGPTEventListener(sseEmitter,traceId);
        apiClient.streamChat(request,chatGPTEventListener,traceId);
        return sseEmitter;
    }
}
