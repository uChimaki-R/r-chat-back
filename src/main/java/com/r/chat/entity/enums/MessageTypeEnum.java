package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    TEXT(0, "普通聊天消息"),
    MEDIA(1, "媒体文件"),
    NOTICE(2, "显示在聊天中间的提示信息");

    @EnumValue
    private final Integer value;
    private final String desc;

    MessageTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
