package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GroupInfoStatusEnum {
    DISBAND(0, "解散"),
    NORMAL(1, "正常");
    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;
    GroupInfoStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
