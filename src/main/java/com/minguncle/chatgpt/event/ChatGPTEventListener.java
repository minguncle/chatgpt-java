package com.minguncle.chatgpt.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * Sse返回处理类
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class ChatGPTEventListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    private String traceId;

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI服务器连接成功!,traceId[{}]", traceId);
    }


    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (data.equals("[DONE]")) {
            log.info("OpenAI服务器发送结束标志!,traceId[{}]", traceId);
            sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]")
                    .reconnectTime(3000));
            return;
        }
        JSONObject jsonObject = JSON.parseObject(data);
        JSONArray choicesJsonArray = jsonObject.getJSONArray("choices");
        String content = null;
        if (choicesJsonArray.isEmpty()) {
            content = "";
        } else {
            JSONObject choiceJson = choicesJsonArray.getJSONObject(0);
            JSONObject deltaJson = choiceJson.getJSONObject("delta");
            String text = deltaJson.getString("content");
            if (text != null) {
                content = text;
                sseEmitter.send(SseEmitter.event()
                        .data(content.trim())
                        .reconnectTime(2000));
            }
        }
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI服务器关闭连接!,traceId[{}]", traceId);
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        log.error("OpenAI服务器连接异常!response：[{}]，traceId[{}]", response, traceId, t);
        eventSource.cancel();
    }
}
