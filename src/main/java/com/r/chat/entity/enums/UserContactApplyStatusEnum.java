package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserContactApplyStatusEnum {
    // 用户联系人申请状态：0：待处理 1：已同意 2：已拒绝 3：已拉黑
    PENDING(0, "待处理"),
    AGREED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    BLOCKED(3, "已拉黑");
    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;

    UserContactApplyStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
