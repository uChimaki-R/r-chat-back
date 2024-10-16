package com.r.chat.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.r.chat.entity.vo.GroupDetailInfoVO;

/**
 * <p>
 * 群聊信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    /**
     * 分页多表联查查询群聊信息，包括群主的名称和群成员数量
     */
    Page<GroupDetailInfoVO> selectGroupDetailInfoPage(Page<GroupDetailInfoVO> page);
}
