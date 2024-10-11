package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserInfoBeautyStatusEnum {
    NOT_USED(0, "未使用"),
    USED(1, "已使用");
    @EnumValue // 标记枚举映射到表中的int值来源
    private final int value;
    private final String desc;
    UserInfoBeautyStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
