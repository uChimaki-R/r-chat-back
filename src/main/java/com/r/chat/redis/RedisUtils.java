package com.r.chat.redis;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.properties.AppProperties;
import com.r.chat.properties.DefaultSysSettingProperties;
import com.r.chat.utils.CastUtils;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
     * 移除用户心跳缓存
     */
    public void removeUserHeartBeat(String userId) {
        if (userId != null) {
            redisOperation.delete(Constants.REDIS_KEY_PREFIX_WS_HEART_BEAT + userId);
        }
    }

    /**
     * 保存token到userTokenInfo的映射，设置一天的过期时间
     */
    public void setToken2UserTokenInfo(String token, UserTokenInfoDTO userTokenInfoDTO) {
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token, userTokenInfoDTO, 1, TimeUnit.DAYS);
    }

    /**
     * 根据用户token获取用户信息
     */
    public UserTokenInfoDTO getUserTokenInfoByToken(String token) {
        return (UserTokenInfoDTO) redisOperation.get(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token);
    }

    /**
     * 根据token移除用户登录信息
     */
    public void removeUserTokenInfoByToken(String token) {
        if (token != null) {
            redisOperation.delete(Constants.REDIS_KEY_PREFIX_USER_TOKEN + token);
        }
    }

    /**
     * 保存用户的联系人id列表
     */
    public void setContactIds(String userId, List<String> contactIds) {
        if (userId == null || contactIds == null || contactIds.isEmpty()) return;
        contactIds.forEach(id -> {
            redisOperation.sAdd(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId, id);
        });
    }

    /**
     * 更新用户的联系人id列表
     */
    public void addToContactIds(String userId, String contactId) {
        redisOperation.sAdd(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId, contactId);
    }

    /**
     * 获取用户的联系人id列表
     */
    public List<String> getContactIds(String userId) {
//        // 为了避免Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.String>'的警告，采用了下面的写法
//        Object o = redisOperation.lRange(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId, 0, -1).get(0);
//        if (o instanceof List<?>) {
//            List<?> list = (List<?>) o;
//            return list.stream().map(oo -> (String) oo).collect(Collectors.toList());
//        }
        // 改用工具类来执行上面的逻辑
        return CastUtils.castList(redisOperation.setMembers(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId), String.class);
    }

    /**
     * 移除指定用户的id列表中的数据
     */
    public void removeFromContactIds(String userId, String idToRemove) {
        if (userId == null || idToRemove == null) return;
        redisOperation.sRemove(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId, idToRemove);
    }

    /**
     * 清空用户的联系人id列表
     */
    public void removeUserContactIds(String userId) {
        if (userId != null) {
            redisOperation.delete(Constants.REDIS_KEY_PREFIX_USER_CONTACT_IDS + userId);
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
