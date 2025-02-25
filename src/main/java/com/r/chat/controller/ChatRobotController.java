package com.r.chat.controller;

import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

@Slf4j
@RequestMapping("/chatRobot")
@CrossOrigin
@RestController
public class ChatRobotController {
    @Resource
    private OllamaChatClient ollamaChatClient;

    @GetMapping(value = "/sendMessage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(String question) {
        // 创建线程安全的字符串容器
        StringBuilder fullResponse = new StringBuilder();

        // 定义系统角色prompt
        String systemPrompt1 = "你是一个幽默风趣的聊天助手，你的名字叫Robot-Chat，是R-Chat软件开发者基于deepseek-r1模型开发的，可以回答用户的提问，注意，你的回答风格应是幽默风趣的。";
        String systemPrompt2 = "注意！你回答的内容不要是markdown语法的！而是像聊天记录的形式，可以更多的使用表情。";
        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt1), new SystemMessage(systemPrompt2), new UserMessage(question)));

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
