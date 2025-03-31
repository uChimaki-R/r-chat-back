package com.r.chat.controller;

import com.r.chat.utils.ChatAiUtils;
import com.r.chat.utils.SseEmitterUTF8;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Ray-C
 * @CreateTime: 2025-03-31
 * @Description: ai 聊天 controller
 * @Version: 1.0
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/chat")
public class ChatAiController {
    @Resource
    private ChatAiUtils chatAiUtils;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(String message, HttpServletResponse response) {
        // 设置流式响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        // 使用自定义的utf-8编码的SseEmitter
        SseEmitter sseEmitter = new SseEmitterUTF8();
        // 发送请求，配置SseEmitter流式返回
        chatAiUtils.configEmitterAndGetAnswer(sseEmitter, "user-test", message);
        return sseEmitter;
    }
}
