package com.minguncle.chatgpt.service.impl;

import com.minguncle.chatgpt.config.Config;
import com.minguncle.chatgpt.event.ChatGPTEventListener;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import com.minguncle.chatgpt.pojo.vo.OriginChatRequest;
import com.minguncle.chatgpt.service.OpenAIChatGPTService;
import com.minguncle.chatgpt.service.SseEventService;
import com.minguncle.chatgpt.utils.api.OpenAIChatGPTApiClient;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;

/**
 * api服务实现类
 *
 * @author wanglinjie
 * @since 2023-03-08 17:09
 */
@Service
public class OpenAIChatGPTServiceImpl implements OpenAIChatGPTService, ApplicationContextAware {


    private OpenAIChatGPTApiClient apiClient;
    @Resource
    private SseEventService sseEventService;


    @Override
    @SneakyThrows
    public String chat(ChatRequest request) {
        OriginChatRequest chatRequest = new OriginChatRequest();
        BeanUtils.copyProperties(request, chatRequest);
        return apiClient.chat(chatRequest);
    }

    /**
     * ChatGPT Proxy
     *
     * @param request
     * @return
     */
    @Override
    public Object completions(ChatRequest request) {
        Optional<String> traceId = Optional.of(request.getTraceId());
        OriginChatRequest chatRequest = new OriginChatRequest();
        BeanUtils.copyProperties(request, chatRequest);
        if (request.getStream()) {
            SseEmitter sseEmitter = sseEventService.getSseEmitter(traceId.get());
            ChatGPTEventListener chatGPTEventListener = new ChatGPTEventListener(sseEmitter, traceId.get(), request);
            apiClient.streamChat(chatRequest, chatGPTEventListener, traceId.get());
            return sseEmitter;
        } else {
            return apiClient.originChat(chatRequest, traceId.get());
        }
    }

    @Override
    @SneakyThrows
    public SseEmitter streamChat(ChatRequest request) {
        Optional<String> traceId = Optional.of(request.getTraceId());
        SseEmitter sseEmitter = sseEventService.getSseEmitter(traceId.get());
        OriginChatRequest chatRequest = new OriginChatRequest();
        BeanUtils.copyProperties(request, chatRequest);
        chatRequest.setStream(true);
        ChatGPTEventListener chatGPTEventListener = new ChatGPTEventListener(sseEmitter, traceId.get(), request);
        apiClient.streamChat(chatRequest, chatGPTEventListener, traceId.get());
        return sseEmitter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Config config = applicationContext.getBean(Config.class);
            String apiKey = config.getApiKey();
            if (apiKey == null || "<YOUR_API_KEY>".equals(apiKey)) {
                throw new RuntimeException("请确认apikey填写正确!");
            }
            String proxyPort = config.getProxyPort();
            String proxyHost = config.getProxyHost();
            if (StringUtils.hasText(proxyHost) || StringUtils.hasText(proxyPort)) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                apiClient = new OpenAIChatGPTApiClient(apiKey, proxy);
                return;
            }
            apiClient = new OpenAIChatGPTApiClient(apiKey);
        } catch (Exception e) {
            throw new RuntimeException("初始化apiClient失败,请检查配置!", e);
        }
    }
}
