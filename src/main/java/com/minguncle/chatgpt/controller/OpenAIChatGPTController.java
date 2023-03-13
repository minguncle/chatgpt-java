package com.minguncle.chatgpt.controller;

import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import com.minguncle.chatgpt.service.OpenAIChatGPTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * OpenAI Chat GPT API接口
 * @author wanglinjie
 * @since 2023-03-08 17:08
 */
@RestController
@CrossOrigin
@Tag(name = "OpenAI Chat GPT API", description = "使用OpenAI的API进行聊天")
public class OpenAIChatGPTController {

    private final OpenAIChatGPTService chatGptService;

    @Autowired
    public OpenAIChatGPTController(OpenAIChatGPTService chatGptService) {
        this.chatGptService = chatGptService;
    }

    /**
     * 返回处理过的字符串
     * @param request
     * @return
     */
    @PostMapping("/chat")
    @Operation(summary = "根据给定的消息生成聊天内容", description = "根据给定的消息生成聊天内容")
    public String chat(@RequestBody ChatRequest request) throws Exception {
        return chatGptService.chat(request);
    }
    /**
     * 流式返回处理过的字符串
     * @param request
     * @return
     */
    @PostMapping( "/chat/stream")
    @Operation(summary = "根据给定的消息生成流式聊天内容", description = "根据给定的消息生成流式聊天内容")
    public void SseEmitterchatStream(@RequestBody ChatRequest request) throws Exception {
        chatGptService.streamChat(request);
    }

    /**
     * ChatGPT Proxy
     * @param request
     * @throws Exception
     */
    @PostMapping("/v1/chat/completions")
    @Operation(summary = "ChatGpt转发接口", description = "ChatGpt转发接口")
    public Object completions(@RequestBody ChatRequest request) throws Exception {
        return chatGptService.completions(request);
    }
}
