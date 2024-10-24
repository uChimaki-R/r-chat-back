package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MessageStatusEnum {
    SENDING(0, "发送中"),
    SENT(1, "已发送");

    @EnumValue
    private final int value;
    private final String desc;

    MessageStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
