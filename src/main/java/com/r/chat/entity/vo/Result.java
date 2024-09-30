package com.r.chat.entity.vo;

import com.r.chat.entity.constants.Constants;
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

    private Integer code; // 状态码
    private String status; // 成功或失败
    private String info; // 显示给用户的提醒信息
    private T data; // 数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = ResponseCodeEnum.SUCCESS.getCode();
        result.status = "success";
        result.info = Constants.MESSAGE_SUCCESS;
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
        result.status = "error";
        result.info = msg;
        return result;
    }

}
