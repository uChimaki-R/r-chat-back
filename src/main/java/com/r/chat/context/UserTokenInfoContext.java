package com.r.chat.context;

import com.r.chat.entity.dto.UserTokenInfoDTO;

public class UserTokenInfoContext {

    public static ThreadLocal<UserTokenInfoDTO> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserTokenInfo(UserTokenInfoDTO userTokenInfoDTO) {
        threadLocal.set(userTokenInfoDTO);
    }

    public static UserTokenInfoDTO getCurrentUserTokenInfo() {
        return threadLocal.get();
    }

    public static void removeCurrentUserTokenInfo() {
        threadLocal.remove();
    }

}
