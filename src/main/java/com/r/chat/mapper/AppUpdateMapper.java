package com.r.chat.mapper;

import com.r.chat.entity.po.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * app更新信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

    /**
     * 根据用户id获取该用户所能获取的最高app版本
     */
    AppUpdate selectLatestForUser(String userId);
}
