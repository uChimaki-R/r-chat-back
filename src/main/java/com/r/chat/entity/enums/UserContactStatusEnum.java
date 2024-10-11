package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserContactStatusEnum {
    // 0：非好友 1：好友 2：已删除好友 3：被好友删除 4：已拉黑好友 5：被好友拉黑
    NOT_FRIENDS(0, "非好友"),
    FRIENDS(1, "好友"),
    DELETED_THE_FRIEND(2, "已删除好友"),
    DELETED_BY_FRIEND(3, "被好友删除"),
    BLOCKED_THE_FRIEND(4, "已拉黑好友"),
    BLOCKED_BY_FRIEND(5, "被好友拉黑");
    @EnumValue
    private final int value;
    private final String desc;

    UserContactStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
