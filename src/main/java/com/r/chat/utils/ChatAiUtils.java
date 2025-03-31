package com.r.chat.utils;

import com.r.chat.entity.enums.AiModeEnum;
import com.r.chat.properties.ChatAiProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Ray-C
 * @CreateTime: 2025-03-31
 * @Description: 对接 Anything LLM 的工具类
 * @Version: 1.0
 */
@Component
@EnableConfigurationProperties(ChatAiProperties.class)
public class ChatAiUtils {
    public static final String ANYTHING_LLM_URI_FORMAT = "http://%s/api/v1/workspace/%s/stream-chat";

    @Resource
    private ChatAiProperties chatAiProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void configEmitterAndGetAnswer(SseEmitter emitter, String sessionId, String message, AiModeEnum mode) {
        // 拼接uri
        String uri = String.format(ANYTHING_LLM_URI_FORMAT, chatAiProperties.getServer(), chatAiProperties.getWorkplaceSlug());
        HttpPost httpPost = new HttpPost(uri);
        // 设置api-key
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + chatAiProperties.getApiKey());
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("mode", mode);
        requestBody.put("sessionId", sessionId);
        try {
            // sse需要新建线程处理
            new Thread(() -> {
                try {
                    httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8));
                    // 发送请求
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    CloseableHttpResponse chatResponse = httpClient.execute(httpPost);
                    // 解析并使用emitter返回
                    BufferedReader reader = new BufferedReader(new InputStreamReader(chatResponse.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 最前面有 'data: ' 占6个位置
                        if (line.length() < 6) {
                            continue;
                        }
                        JsonNode responseNode = objectMapper.readTree(line.substring(6));
                        // 处理文本响应块
                        if ("textResponseChunk".equals(responseNode.get("type").asText())) {
                            String chunk = responseNode.get("textResponse").asText();
                            emitter.send(chunk);
                            System.out.print(chunk);
                        }
                        // 处理最终响应
                        if ("finalizeResponseStream".equals(responseNode.get("type").asText())) {
                            emitter.send("DONE");
                            System.out.println();
                            break;
                        }
                    }
                    emitter.complete(); // 完成SSE连接
                    httpClient.close(); // 关闭client
                } catch (Exception e) {
                    emitter.completeWithError(e); // 处理错误
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}