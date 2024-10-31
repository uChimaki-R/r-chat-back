package com.r.chat.context;

import com.r.chat.entity.dto.UserTokenInfoDTO;

public class UserTokenInfoContext {

    public static ThreadLocal<UserTokenInfoDTO> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserTokenInfo(UserTokenInfoDTO userTokenInfo) {
        threadLocal.set(userTokenInfo);
    }

    public static UserTokenInfoDTO getCurrentUserTokenInfo() {
        return threadLocal.get();
    }

    public static String getCurrentUserId() {
        return threadLocal.get().getUserId();
    }

    public static String getCurrentUserNickName() {
        return threadLocal.get().getNickName();
    }

    public static String getCurrentUserToken() {
        return threadLocal.get().getToken();
    }

    public static void removeCurrentUserInfo() {
        threadLocal.remove();
    }

}
