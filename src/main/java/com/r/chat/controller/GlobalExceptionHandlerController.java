package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.ResponseCodeEnum;
import com.r.chat.entity.result.Result;
import com.r.chat.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        if (e instanceof NoHandlerFoundException) {
            // 404
            Result<String> result = Result.error(ResponseCodeEnum.NOT_FOUND.getCode(), Constants.MESSAGE_NOT_FOUND);
            log.error("NoHandlerFoundException | {}", result);
            return result;
        }else if (e instanceof BusinessException) {
            // 业务异常
            BusinessException be = (BusinessException) e;
            Result<String> result = Result.error(be.getCode(), be.getMessage());
            log.error("BusinessException | {}", result);
            return result;
        } else if (e instanceof ConstraintViolationException) {
            // 参数错误
            ConstraintViolationException cve = (ConstraintViolationException) e;
            String[] ms = cve.getMessage().split(" ");
            Result<String> result = Result.error(ResponseCodeEnum.PARAMETERS_ERROR.getCode(), ms[ms.length - 1]);
            log.error("ConstraintViolationException | {}", result);
            return result;
        }
        else {
            // 内部错误
            Result<String> result = Result.error(ResponseCodeEnum.INTERNAL_ERROR.getCode(), Constants.MESSAGE_INTERNAL_ERROR);
            log.error("Error | {}", result);
            return result;
        }
    }
}
