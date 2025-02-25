package com.r.chat.controller;

import com.r.chat.properties.DefaultSysSettingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("/chatRobot")
@CrossOrigin
@RestController
public class ChatRobotController {
    @Resource
    private OllamaChatClient ollamaChatClient;
    @Resource
    private DefaultSysSettingProperties defaultSysSettingProperties;

    @GetMapping(value = "/sendMessage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(String question) {
        // 创建线程安全的字符串容器
        StringBuilder fullResponse = new StringBuilder();

        // prompt
        // 系统prompt
        List<Message> messages = new ArrayList<>();
        defaultSysSettingProperties.getSystemPrompts().stream().map(SystemMessage::new).forEach(messages::add);
        // 用户prompt
        messages.add(new UserMessage(question));
        Prompt prompt = new Prompt(messages);

        // 获取原始响应流
        Flux<String> originalResponse = ollamaChatClient.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .doOnNext(content -> {
                    // 收集每个流片段
                    synchronized (fullResponse) {
                        fullResponse.append(content);
                    }
                });


        // 添加完成标识和保存逻辑
        return originalResponse
                .concatWithValues("[DONE]")
                .doOnComplete(() -> {
                    // todo 异步保存聊天记录、修改会画信息等
                    Mono.fromRunnable(() -> {
                        try {
                            String answer = fullResponse.toString();
                            log.info("Send answer: {}", answer);
                        } catch (Exception e) {
                            log.error("保存聊天记录失败", e);
                        }
                    }).subscribeOn(Schedulers.boundedElastic()).subscribe();
                });
    }
}
