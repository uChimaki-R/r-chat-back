package com.r.chat.context;

import com.r.chat.entity.vo.UserInfoToken;

public class UserInfoTokenContext {

    public static ThreadLocal<UserInfoToken> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserInfoToken(UserInfoToken userInfoToken) {
        threadLocal.set(userInfoToken);
    }

    public static UserInfoToken getCurrentUserInfoToken() {
        return threadLocal.get();
    }

    public static void removeCurrentUserInfoToken() {
        threadLocal.remove();
    }

}
