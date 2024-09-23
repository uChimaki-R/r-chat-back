package com.r.chat.service;

import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.entity.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
public interface IUserInfoService extends IService<UserInfo> {

    /**
     * 注册账号
     */
    void register(RegisterDTO registerDTO);
}
