package com.r.chat.context;

import com.r.chat.entity.dto.UserTokenInfoDTO;

public class UserInfoTokenContext {

    public static ThreadLocal<UserTokenInfoDTO> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserInfoToken(UserTokenInfoDTO userTokenInfoDTO) {
        threadLocal.set(userTokenInfoDTO);
    }

    public static UserTokenInfoDTO getCurrentUserInfoToken() {
        return threadLocal.get();
    }

    public static void removeCurrentUserInfoToken() {
        threadLocal.remove();
    }

}
