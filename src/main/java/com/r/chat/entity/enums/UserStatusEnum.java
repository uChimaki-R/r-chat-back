package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ENABLE(0, "启用"),
    DISABLED(1, "禁用");
    @EnumValue
    private final int value;
    private final String desc;
    UserStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
