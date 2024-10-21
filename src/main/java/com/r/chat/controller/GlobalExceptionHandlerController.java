package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.ResponseCodeEnum;
import com.r.chat.entity.result.Result;
import com.r.chat.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        if (e instanceof NoHandlerFoundException) {
            // 404
            Result<String> result = Result.error(ResponseCodeEnum.NOT_FOUND.getCode(), Constants.MESSAGE_NOT_FOUND);
            log.warn("NoHandlerFoundException | {}", result);
            return result;
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            // 请求方式不支持
            Result<String> result = Result.error(ResponseCodeEnum.REJECTED.getCode(), Constants.MESSAGE_REQUEST_METHOD_ERROR);
            log.warn("HttpRequestMethodNotSupportedException | {}", result);
            return result;
        } else if (e instanceof BusinessException) {
            // 业务异常
            BusinessException be = (BusinessException) e;
            Result<String> result = Result.error(be.getCode(), be.getMessage());
            log.warn("BusinessException | {}", result);
            return result;
        } else if (e.getClass().getPackage().getName().startsWith("org.springframework.validation") ||
                e.getClass().getPackage().getName().startsWith("javax.validation")) {
            // 验证数据格式错误
            String[] ms = e.getMessage().split(" ");
            String message = ms[ms.length - 1];
            if (message.startsWith("[")) message = message.substring(1, message.length() - 1);
            if (message.endsWith("]")) message = message.substring(0, message.length() - 1);
            Result<String> result = Result.error(ResponseCodeEnum.PARAMETERS_ERROR.getCode(), message);
            log.warn("ValidationException | {}", result);
            return result;
        } else if (e instanceof BindException) {
            // 绑定错误，可能是枚举没匹配到，或者是Long传了String等
            Result<String> result = Result.error(ResponseCodeEnum.PARAMETERS_ERROR.getCode(), Constants.MESSAGE_PARAMETER_ERROR);
            log.warn("BindException | {}", result);
            return result;
        } else {
            // 内部错误
            Result<String> result = Result.error(ResponseCodeEnum.INTERNAL_ERROR.getCode(), Constants.MESSAGE_INTERNAL_ERROR);
            log.error("UnexpectedException | {}", result);
            return result;
        }
    }
}
