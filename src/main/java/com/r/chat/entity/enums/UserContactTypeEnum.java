package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserContactTypeEnum {
    FRIENDS(0, "好友"),
    GROUP(1, "群聊");
    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;

    UserContactTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
