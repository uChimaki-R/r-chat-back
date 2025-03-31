package com.r.chat.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

/**
 * @Author: Ray-C
 * @CreateTime: 2025-03-31
 * @Description: 修改响应数据编码为UTF-8，从而避免浏览器乱码
 * @Version: 1.0
 */
public class SseEmitterUTF8 extends SseEmitter {
    @Override
    protected void extendResponse(ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);

        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType(new MediaType(MediaType.TEXT_EVENT_STREAM, StandardCharsets.UTF_8));
    }
}