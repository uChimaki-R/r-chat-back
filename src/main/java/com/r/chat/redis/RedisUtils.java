package com.r.chat.redis;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.properties.AppProperties;
import com.r.chat.properties.DefaultSysSettingProperties;
import com.r.chat.utils.CopyUtils;
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
    private final DefaultSysSettingProperties defaultSysSettingProperties;
    private final AppProperties appProperties;

    /**
     * 获取用户心跳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisOperation.get(Constants.REDIS_KEY_PREFIX_WS_HEART_BEAT + userId);
    }

    /**
     * 保存用户心跳
     */
    public void setUserHeartBeat(String userId) {
        // 值为当前系统时间
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_WS_HEART_BEAT + userId, System.currentTimeMillis(), appProperties.getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    /**
     * 保存token到id的映射，设置一天的过期时间
     */
    public void setToken2UserId(String token, String userId) {
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token, userId, 1, TimeUnit.DAYS);
    }

    /**
     * 根据用户token获取id
     */
    public String getUserIdByToken(String token) {
        return (String) redisOperation.get(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token);
    }

    /**
     * 根据用户id移除用户登录信息token
     */
    public void removeTokenByUserId(String userId) {
        String token = getUserIdByToken(userId);
        if (token != null) {
            redisOperation.delete(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token);
        }
    }

    /**
     * 获取系统设置信息，如果没有则从配置文件中获取默认系统设置信息
     */
    public SysSettingDTO getSysSetting() {
        SysSettingDTO sysSettingDTO = (SysSettingDTO) redisOperation.get(Constants.REDIS_KEY_SYS_SETTINGS);
        if (sysSettingDTO == null) {
            // 获取默认配置
            sysSettingDTO = CopyUtils.copyBean(defaultSysSettingProperties, SysSettingDTO.class);
        }
        return sysSettingDTO;
    }

    /**
     * 保存系统设置缓存
     */
    public void setSysSetting(SysSettingDTO sysSettingDTO) {
        // 只更新非空的信息
        SysSettingDTO target = getSysSetting();  // 原来的信息作为target
        CopyUtils.copyPropertiesIgnoreNull(sysSettingDTO, target);  // 把新的信息中非null值拷贝到target
        redisOperation.set(Constants.REDIS_KEY_SYS_SETTINGS, target);
    }
}
