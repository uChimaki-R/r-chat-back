package com.r.chat.utils;

import com.r.chat.context.UserIdContext;

public class VerifyUtils {
    /**
     * 判断传入的用户id是否和当前的请求用户id相同，排除使用api操作他人数据的情况
     */
    public static boolean isCurrentUser(String userId) {
        return UserIdContext.getCurrentUserId().equals(userId);
    }
}
