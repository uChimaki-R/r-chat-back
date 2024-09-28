package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserContactTypeEnum {
    FRIENDS(0, "好友"),
    GROUP(1, "群组");
    @EnumValue
    private final int value;
    private final String desc;

    UserContactTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
