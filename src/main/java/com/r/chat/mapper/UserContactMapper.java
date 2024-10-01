package com.r.chat.mapper;

import com.r.chat.entity.dto.GroupMemberInfoDTO;
import com.r.chat.entity.po.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 用户联系人 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
public interface UserContactMapper extends BaseMapper<UserContact> {

    /**
     * 根据群聊id获取群成员的id、昵称信息
     */
    List<GroupMemberInfoDTO> selectGroupMemberByGroupId(String groupId);
}
