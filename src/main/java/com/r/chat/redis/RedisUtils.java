package com.r.chat.redis;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.properties.DefaultSysSettingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 对redis操作再进一步的包装
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {
    private final RedisOperation redisOperation;
    private final DefaultSysSettingProperties defaultSysSettingProperties;

    /**
     * 获取用户心跳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisOperation.get(Constants.REDIS_KEY_PREFIX_WS_HEART_BEAT + userId);
    }

    /**
     * 保存用户token信息
     */
    public void saveUserTokenInfo(UserTokenInfoDTO userTokenInfoDTO) {
        // 保存两份信息，一份是用token找到用户信息，一份是userId找到token
        // 保存一天的登录信息
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_USER_TOKEN + userTokenInfoDTO.getToken(), userTokenInfoDTO, 1, TimeUnit.DAYS);
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_USER_ID + userTokenInfoDTO.getUserId(), userTokenInfoDTO.getToken(), 1, TimeUnit.DAYS);
    }

    /**
     * 根据token获取用户信息
     */
    public UserTokenInfoDTO getUserTokenInfo(String token) {
        return (UserTokenInfoDTO) redisOperation.get(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token);
    }

    /**
     * 根据用户id获取token
     */
    public String getUserToken(String userId) {
        return (String) redisOperation.get(Constants.REDIS_KEY_PREFIX_USER_ID + userId);
    }

    /**
     * 获取系统设置信息，如果没有则从配置文件中获取默认系统设置信息
     */
    public SysSettingDTO getSysSetting() {
        SysSettingDTO sysSettingDTO = (SysSettingDTO) redisOperation.get(Constants.REDIS_KEY_SYS_SETTINGS);
        if (sysSettingDTO == null) {
            // 获取默认配置
            sysSettingDTO = new SysSettingDTO();
            BeanUtils.copyProperties(defaultSysSettingProperties, sysSettingDTO);
        }
        return sysSettingDTO;
    }
}
