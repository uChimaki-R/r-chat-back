package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AppUpdateStatusEnum {
    UNPUBLISHED(0, "未发布"),
    GRAYSCALE_RELEASE(1, "灰度发布"),
    FULL_RELEASE(2, "全网发布");

    @EnumValue
    private final int code;
    private final String desc;

    AppUpdateStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
