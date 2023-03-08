package com.minguncle.chatgpt.controller;

import com.minguncle.chatgpt.service.SseEventService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * SSE接口
 *
 * @author wanglinjie
 * @since 2023-03-08 17:08
 */
@RestController()
@RequestMapping("/sse")
@CrossOrigin
public class SseEventController {
    @Resource
    SseEventService sseEventService;

    @GetMapping(value = "/connect/{userId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter connect(@PathVariable("userId") String userId) throws Exception {
        return sseEventService.connect(userId);
    }
}
