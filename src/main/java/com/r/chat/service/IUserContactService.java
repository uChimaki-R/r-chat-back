package com.r.chat.service;

import com.r.chat.entity.dto.GroupMemberInfoDTO;
import com.r.chat.entity.po.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户联系人 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
public interface IUserContactService extends IService<UserContact> {

    /**
     * 获取群聊的群成员信息
     */
    List<GroupMemberInfoDTO> getGroupMemberInfo(String groupId);
}
