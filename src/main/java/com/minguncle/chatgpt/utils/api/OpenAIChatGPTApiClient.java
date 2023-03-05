package com.minguncle.chatgpt.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

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
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(30L, TimeUnit.SECONDS);
        okHttpClient = client.build();
    }

    public OpenAIChatGPTApiClient(String apiKey, Proxy proxy) {
        this.apiKey = apiKey;
        this.proxy = proxy;
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(30L, TimeUnit.SECONDS);
        client.proxy(this.proxy);
        okHttpClient = client.build();
    }


    public String chat(ChatRequest request) {
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

    public void streamChat(ChatRequest request, EventSourceListener eventSourceListener, String traceId) {
        try {
            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
            Request apiRequest = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(JSON.toJSONString(request), MediaType.parse("application/json")))
                    .header("Authorization", "Bearer " + this.apiKey)
                    .build();
            EventSource eventSource = factory.newEventSource(apiRequest, eventSourceListener);
        } catch (Exception e) {
            log.error("请求服务器异常!：traceId{}", traceId, e);
        }
    }
}




