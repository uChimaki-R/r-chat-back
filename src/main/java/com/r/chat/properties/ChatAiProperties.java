package com.r.chat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: Ray-C
 * @CreateTime: 2025-03-31
 * @Description: AnythingLLM 配置属性
 * @Version: 1.0
 */
@Data
@ConfigurationProperties("r.chat.ai")
public class ChatAiProperties {
    private String server;
    private String apiKey;
    private String workplaceSlug;
}
