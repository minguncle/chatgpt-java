package com.minguncle.chatgpt.service.impl;

import com.minguncle.chatgpt.event.ChatGPTEventListener;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import com.minguncle.chatgpt.pojo.vo.OriginChatRequest;
import com.minguncle.chatgpt.service.OpenAIChatGPTService;
import com.minguncle.chatgpt.service.SseEventService;
import com.minguncle.chatgpt.utils.api.OpenAIChatGPTApiClient;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * api服务实现类
 *
 * @author wanglinjie
 * @since 2023-03-08 17:09
 */
@Service
public class OpenAIChatGPTServiceImpl implements OpenAIChatGPTService {


    private static OpenAIChatGPTApiClient apiClient;
    @Resource
    private SseEventService sseEventService;

    @Autowired
    public static void setApiClient(OpenAIChatGPTApiClient apiClient) {
        OpenAIChatGPTServiceImpl.apiClient = apiClient;
    }

    @Override
    @SneakyThrows
    public String chat(ChatRequest request) {
        OriginChatRequest chatRequest = new OriginChatRequest();
        BeanUtils.copyProperties(request, chatRequest);
        return apiClient.chat(chatRequest);
    }

    @Override
    @SneakyThrows
    public void streamChat(ChatRequest request) {
        Optional<String> userID = Optional.of(request.getUserID());
        SseEmitter sseEmitter = sseEventService.getSseEmitter(userID.get());
        OriginChatRequest chatRequest = new OriginChatRequest();
        BeanUtils.copyProperties(request, chatRequest);
        chatRequest.setStream(true);
        ChatGPTEventListener chatGPTEventListener = new ChatGPTEventListener(sseEmitter, userID.get());
        apiClient.streamChat(chatRequest, chatGPTEventListener, userID.get());
    }
}
