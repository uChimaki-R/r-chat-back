package com.r.chat.controller;

import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.AiModeEnum;
import com.r.chat.exception.ParameterErrorException;
import com.r.chat.utils.ChatAiUtils;
import com.r.chat.utils.SseEmitterUTF8;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
    public SseEmitter chat(@RequestParam String message, @RequestParam Integer mode, HttpServletResponse response) {
        AiModeEnum aiModeEnum = AiModeEnum.getAiModeEnum(mode);
        if (aiModeEnum == null) {
            throw new ParameterErrorException(Constants.MESSAGE_ENUM_ERROR);
        }
        log.info("Ai chat | sessionId: {}, message: {}, mode: {}", UserTokenInfoContext.getCurrentUserId(), message, aiModeEnum);
        // 设置流式响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        // 使用自定义的utf-8编码的SseEmitter
        SseEmitter sseEmitter = new SseEmitterUTF8();
        // 发送请求，配置SseEmitter流式返回
        chatAiUtils.configEmitterAndGetAnswer(sseEmitter, UserTokenInfoContext.getCurrentUserId(), message, aiModeEnum);
        return sseEmitter;
    }
}
