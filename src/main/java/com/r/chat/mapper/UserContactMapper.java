package com.r.chat.mapper;

import com.r.chat.entity.dto.BasicInfoDTO;
import com.r.chat.entity.po.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 用户联系人 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {

    /**
     * 根据群聊id获取群成员的id、昵称信息
     */
    List<BasicInfoDTO> selectGroupMemberByGroupId(String groupId);

    /**
     * 根据userId获取好友信息
     */
    List<BasicInfoDTO> selectUserFriends(String userId);

    /**
     * 根据userId获取加入的群聊信息
     */
    List<BasicInfoDTO> selectGroupFriends(String userId);
}
