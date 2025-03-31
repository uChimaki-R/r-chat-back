package com.r.chat.entity.enums;

/**
 * @Author: Ray-C
 * @CreateTime: 2025-03-31
 * @Description: ai调用类型枚举
 * @Version: 1.0
 */
public enum AiModeEnum {
    CHAT(1, "聊天"),
    QUERY(0, "查询");

    public final int value;
    public final String name;

    AiModeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AiModeEnum getAiModeEnum(Integer value) {
        for (AiModeEnum aiMode : AiModeEnum.values()) {
            if (aiMode.value == value) {
                return aiMode;
            }
        }
        return null;
    }
}
