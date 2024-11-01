package com.r.chat.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum FileTypeEnum {
    IMAGE(0, "图片文件"),
    VIDEO(1, "视频文件"),
    OTHER(2, "其他文件");

    @EnumValue
    private final int value;
    private final String desc;
    FileTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
