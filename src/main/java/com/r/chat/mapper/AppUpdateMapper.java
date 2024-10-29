package com.r.chat.mapper;

import com.r.chat.entity.po.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * app更新信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
@Mapper
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

    /**
     * 根据用户id获取该用户所能获取的最高app版本
     */
    AppUpdate selectLatestForUser(String userId);
}
