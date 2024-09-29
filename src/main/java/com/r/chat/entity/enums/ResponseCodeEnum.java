package com.r.chat.entity.enums;

import lombok.Getter;

@Getter
public enum ResponseCodeEnum {
    SUCCESS(200),  // 正常请求
    NOT_FOUND(404),  // 请求地址不存在
    PARAMETERS_ERROR(600),  // 参数错误
    ALREADY_EXIST(601),  // 信息已存在
    REJECTED(602),  // 无法/拒绝获取该信息
    UNAUTHORIZED(901),  // 未登录
    INTERNAL_ERROR(500);  // 内部错误

    private final int code;

    ResponseCodeEnum(int code) {
        this.code = code;
    }

}