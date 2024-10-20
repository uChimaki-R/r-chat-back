package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AppUpdateMethodTypeEnum {
    FILE(0, "文件"),
    OUTER_LINK(1, "外链");

    @EnumValue
    private final int code;
    private final String desc;

    AppUpdateMethodTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
