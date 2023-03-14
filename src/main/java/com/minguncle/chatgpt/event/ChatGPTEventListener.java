package com.minguncle.chatgpt.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import com.minguncle.chatgpt.service.impl.SseEventServiceImpl;
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
@NoArgsConstructor
public class ChatGPTEventListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    private String traceId;

    private ChatRequest params;
    private static final StringBuffer lastContent = new StringBuffer("");

    public ChatGPTEventListener(SseEmitter sseEmitter, String traceId, ChatRequest params) {
        this.sseEmitter = sseEmitter;
        this.traceId = traceId;
        this.params = params;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI服务器连接成功!,traceId[{}]", traceId);
    }


    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (data.equals("[DONE]")) {
            log.info("OpenAI服务器发送结束标志!,content:[{}],traceId:[{}]", lastContent,traceId);
            sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]")
                    .reconnectTime(3000));
            SseEventServiceImpl.remove(traceId);
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
                log.debug("接受消息:[{}],traceId[{}]", content.trim(),traceId);
                sseEmitter.send(SseEmitter.event()
                        .data(data)
                        .reconnectTime(2000));
                lastContent.append(text);
            }
        }
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI服务器关闭连接!,traceId[{}]", traceId);
        SseEventServiceImpl.closeAndRemove(traceId);
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        log.error("OpenAI服务器连接异常!response：[{}]，traceId[{}]", response, traceId, t);
        eventSource.cancel();
        SseEventServiceImpl.closeAndRemove(traceId);
    }
}
