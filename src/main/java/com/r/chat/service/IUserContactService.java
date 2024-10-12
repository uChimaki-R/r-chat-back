package com.r.chat.service;

import com.r.chat.entity.dto.*;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
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
    List<BasicInfoDTO> getGroupMemberInfo(String groupId);

    /**
     * 搜索联系人
     */
    ContactSearchResultDTO search(String contactId);

    /**
     * 请求添加联系人
     */
    JoinTypeEnum applyAdd(ApplyDTO applyDTO);

    /**
     * 获取好友或加入的群聊的信息
     */
    List<BasicInfoDTO> loadContact(ContactTypeDTO contactTypeDTO);

    /**
     * 添加联系人
     */
    void addContact(ContactApplyAddDTO contactApplyAddDTO);

    /**
     * 获取联系人基础信息（名片）
     */
    ContactBasicInfoDTO getContactBasicInfo(String contactId);

    /**
     * 获取联系人详细信息
     */
    ContactDetailInfoDTO getContactDetailInfo(String contactId);

    /**
     * 根据status执行删除/拉黑联系人的逻辑
     */
    void removeContact(String contactId, UserContactStatusEnum status);

    /**
     * 为二者添加相互的联系人关系
     */
    void addMutualContact(String fromId, String toId, UserContactStatusEnum status);
}
