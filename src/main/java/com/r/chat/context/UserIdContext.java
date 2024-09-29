package com.r.chat.context;

import com.r.chat.entity.vo.UserInfoToken;

public class UserIdContext {

    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(String userId) {
        threadLocal.set(userId);
    }

    public static String getCurrentUserId() {
        return threadLocal.get();
    }

    public static void removeCurrentUserId() {
        threadLocal.remove();
    }

}
