package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum GroupMemberOpTypeEnum {
    ADD(0, "加入群聊"),
    REMOVE(1, "移出群聊");

    @EnumValue
    private final Integer value;
    private final String desc;

    GroupMemberOpTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
