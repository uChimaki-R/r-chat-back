package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.ResponseCodeEnum;
import com.r.chat.result.Result;
import com.r.chat.exception.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        if (e instanceof NoHandlerFoundException) {
            // 404
            return Result.error(ResponseCodeEnum.NOT_FOUND.getCode(), Constants.MESSAGE_NOT_FOUND);
        }else if (e instanceof BusinessException) {
            // 业务异常
            BusinessException be = (BusinessException) e;
            return Result.error(be.getCode(), be.getMessage());
        } else {
            // 内部错误
            return Result.error(ResponseCodeEnum.INTERNAL_ERROR.getCode(), Constants.MESSAGE_INTERNAL_ERROR);
        }
    }
}
