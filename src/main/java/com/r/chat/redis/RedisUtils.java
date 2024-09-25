package com.r.chat.redis;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 对redis操作再进一步的包装
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {
    private final RedisOperation redisOperation;

    /**
     * 获取用户心跳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisOperation.get(Constants.REDIS_KEY_WS_HEART_BEAT_PREFIX + userId);
    }

    /**
     * 保存用户token信息
     */
    public void saveUserTokenInfo(UserInfoVO userInfoVO) {
        // 保存两份信息，一份是用token找到用户信息，一份是userId找到token
        // 保存一天的登录信息
        redisOperation.setEx(Constants.REDIS_KEY_USER_TOKEN_PREFIX + userInfoVO.getToken(), userInfoVO, 1, TimeUnit.DAYS);
        redisOperation.setEx(Constants.REDIS_KEY_USER_ID_PREFIX + userInfoVO.getUserId(), userInfoVO.getToken(), 1, TimeUnit.DAYS);
    }
}
