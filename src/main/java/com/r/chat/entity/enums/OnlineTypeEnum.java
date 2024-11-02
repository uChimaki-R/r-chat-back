package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OnlineTypeEnum {
    ONLINE(0, "在线"),
    OFFLINE(1, "离线");
    @EnumValue
    private final int value;
    private final String desc;

    OnlineTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
