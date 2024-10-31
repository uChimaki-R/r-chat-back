package com.r.chat.utils;

import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.IllegalOperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyUtils {
    /**
     * 断言传入的用户id和当前的请求用户id相同，不同时会抛出异常
     * 以此排除使用api操作他人数据的情况
     */
    public static void assertIsCurrentUser(String userId) {
        if (!UserTokenInfoContext.getCurrentUserId().equals(userId)) {
            log.warn("非法操作 合法操作者userId: {}", userId);
            throw new IllegalOperationException(Constants.MESSAGE_ILLEGAL_OPERATION);
        }
    }
}
