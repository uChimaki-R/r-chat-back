package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum JoinTypeEnum {
    JOIN_DIRECTLY(0, "直接添加"),
    JOIN_WITH_CONFIRMATION(1, "需要同意后才能添加");
    @EnumValue
    private final int value;
    private final String desc;

    JoinTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
