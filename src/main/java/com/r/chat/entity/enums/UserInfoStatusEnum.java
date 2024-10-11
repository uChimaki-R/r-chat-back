package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserInfoStatusEnum {
    ENABLE(0, "启用"),
    DISABLED(1, "禁用");
    @EnumValue
    private final int value;
    private final String desc;
    UserInfoStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
