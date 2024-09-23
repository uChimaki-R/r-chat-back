package com.r.chat.controller;

import com.r.chat.entity.enums.ResponseCodeEnum;
import com.r.chat.entity.vo.Result;
import com.r.chat.exception.BusinessException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;

@RestControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        if (e instanceof NoHandlerFoundException) {
            // 404
            return Result.error(ResponseCodeEnum.NOT_FOUND.getCode(), ResponseCodeEnum.NOT_FOUND.getMessage());
        }
        else if (e instanceof BindException || e instanceof MethodArgumentNotValidException || e instanceof HttpRequestMethodNotSupportedException) {
            // 拒接请求
            return Result.error(ResponseCodeEnum.NOT_ACCEPTABLE.getCode(), ResponseCodeEnum.NOT_ACCEPTABLE.getMessage());
        }
        else if (e instanceof BusinessException) {
            // 业务异常
            BusinessException be = (BusinessException) e;
            if (be.getCode() != null) {
                return Result.error(be.getCode(), be.getMessage());
            }
            return Result.error(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode(), be.getMessage());
        }
        else {
            return Result.error(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode(), ResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        }
    }
}
