package com.r.chat.service;

import com.r.chat.entity.dto.BeautyUserInfoDTO;
import com.r.chat.entity.po.BeautyUserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息（靓号） 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
public interface IBeautyUserInfoService extends IService<BeautyUserInfo> {

    /**
     * 新增或更新靓号信息
     */
    void saveOrUpdateBeautyAccount(BeautyUserInfoDTO beautyUserInfoDTO);
}
