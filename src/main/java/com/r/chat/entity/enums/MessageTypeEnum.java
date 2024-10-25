package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    CHAT(0, "普通聊天消息"),

    FILE_UPLOAD(1, "媒体文件"),;

    @EnumValue
    private final Integer value;
    private final String desc;

    MessageTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
