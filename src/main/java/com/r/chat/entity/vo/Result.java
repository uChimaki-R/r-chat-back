package com.r.chat.entity.vo;

import com.r.chat.entity.enums.ResponseCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 *
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 编码：200成功，0和其它数字为失败
    private String status;
    private String info; // 信息
    private T data; // 数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = ResponseCodeEnum.SUCCESS.getCode();
        result.status = ResponseCodeEnum.SUCCESS.getMessage();
        result.info = "请求成功";
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = success();
        result.data = object;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.status = "Error";
        result.info = msg;
        return result;
    }

}
