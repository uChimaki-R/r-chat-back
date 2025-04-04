package com.r.chat.service;

import com.r.chat.entity.dto.AppUpdateDTO;
import com.r.chat.entity.dto.AppUpdateReleaseDTO;
import com.r.chat.entity.po.AppUpdate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.r.chat.entity.vo.AppUpdateVO;

/**
 * <p>
 * app更新信息 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
public interface IAppUpdateService extends IService<AppUpdate> {

    /**
     * 新增或更新app更新信息
     */
    void saveOrUpdateAppUpdate(AppUpdateDTO appUpdateDTO);

    /**
     * 发布app更新
     */
    void releaseUpdate(AppUpdateReleaseDTO appUpdateReleaseDTO);

    /**
     * 删除app更新信息
     */
    void delUpdate(Integer id);

    /**
     * 检查版本更新，获取用户可用的最高版本信息
     */
    AppUpdateVO checkVersion(String version);
}
