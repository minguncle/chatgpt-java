package com.minguncle.chatgpt.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.minguncle.chatgpt.pojo.vo.OriginChatRequest;
import com.minguncle.chatgpt.service.impl.SseEventServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.util.Assert;

import java.net.Proxy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OpenAIChatGPTApiClient {

    private String apiKey;

    private Proxy proxy;

    private OkHttpClient okHttpClient;

    private static final String URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIChatGPTApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30L, TimeUnit.SECONDS)
                .build();
    }

    public OpenAIChatGPTApiClient(String apiKey, Proxy proxy) {
        this.apiKey = apiKey;
        this.proxy = proxy;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30L, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .build();
    }


    /**
     * chat，返回处理完的text
     *
     * @param request
     * @return
     */
    public String chat(OriginChatRequest request) {
        try {
            RequestBody body = RequestBody.create(JSON.toJSONString(request), MediaType.parse("application/json"));
            Request apiRequest = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .header("Authorization", "Bearer " + apiKey)
                    .build();
            try (Response response = okHttpClient.newCall(apiRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("API request failed with response code: " + response.code());
                }
                //处理返回
                JSONObject jsonObject = JSON.parseObject(Objects.requireNonNull(response.body()).string());
                JSONArray choicesJsonArray = jsonObject.getJSONArray("choices");
                if (choicesJsonArray.isEmpty()) {
                    return "";
                } else {
                    JSONObject choiceJson = choicesJsonArray.getJSONObject(0);
                    JSONObject messageJson = choiceJson.getJSONObject("message");
                    String text = messageJson.getString("content");
                    return text.trim();
                }
            }
        } catch (Exception e) {
            log.error("请求服务器异常!：msg{}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * chat proxy，直接返回原始数据
     *
     * @param request
     * @return
     */
    public String originChat(OriginChatRequest request,String traceId) {
        try {
            RequestBody body = RequestBody.create(JSON.toJSONString(request), MediaType.parse("application/json"));
            Request apiRequest = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .header("Authorization", "Bearer " + apiKey)
                    .build();
            try (Response response = okHttpClient.newCall(apiRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("API request failed with response code: " + response.code());
                }
                //直接返回
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (Exception e) {
            log.error("请求服务器异常!：msg[{}],traceId: [{}]", e.getMessage(),traceId, e);
        }
        return "";
    }


    /**
     * 流式请求openapi接口
     *
     * @param request
     * @param eventSourceListener
     * @param traceId
     */
    public void streamChat(OriginChatRequest request, EventSourceListener eventSourceListener, String traceId) {
        try {
            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
            Request apiRequest = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(JSON.toJSONString(request), MediaType.parse("application/json")))
                    .header("Authorization", "Bearer " + this.apiKey)
                    .header("Content-type", "application/octet-stream")
                    .build();
            EventSource eventSource = factory.newEventSource(apiRequest, eventSourceListener);
        } catch (Exception e) {
            log.error("请求服务器异常!：msg[{}],traceId: [{}]", e.getMessage(),traceId, e);
            SseEventServiceImpl.closeAndRemove(traceId);
        }
    }


    /**
     * 流式请求openapi接口
     *
     * @param request
     * @param eventSourceListener
     * @param traceId
     */
    public void completion(OriginChatRequest request, EventSourceListener eventSourceListener, String traceId) {
        try {
            if (request.getStream()) {
                Assert.notNull(eventSourceListener);
                streamChat(request, eventSourceListener, traceId);
            } else {
                originChat(request,traceId);
            }
        } catch (Exception e) {
            log.error("请求服务器异常!：traceId{}", traceId, e);
        }
    }


}




