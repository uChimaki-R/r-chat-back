package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.ContactSearchResultDTO;
import com.r.chat.entity.dto.GroupMemberInfoDTO;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.GroupInfo;
import com.r.chat.entity.po.UserContact;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.exception.GroupNotExistException;
import com.r.chat.exception.UserNotExistException;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户联系人 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements IUserContactService {
    private final UserContactMapper userContactMapper;
    private final UserInfoMapper userInfoMapper;
    private final GroupInfoMapper groupInfoMapper;

    @Override
    public List<GroupMemberInfoDTO> getGroupMemberInfo(String groupId) {
        return userContactMapper.selectGroupMemberByGroupId(groupId);
    }

    @Override
    public ContactSearchResultDTO search(String contactId) {
        ContactSearchResultDTO contactSearchResultDTO = new ContactSearchResultDTO();
        // 先根据传入的id前缀判断是打算加用户还是加群聊
        IdPrefixEnum prefix = IdPrefixEnum.getByPrefix(contactId.charAt(0));
        if (prefix == null) {
            // id输错，默认报用户不存在
            log.warn("搜索id前缀错误");
            throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        contactSearchResultDTO.setContactType(
                prefix == IdPrefixEnum.USER ? UserContactTypeEnum.FRIENDS : UserContactTypeEnum.GROUP
        );
        contactSearchResultDTO.setContactId(contactId);
        switch (prefix) {
            case USER:
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.lambda().eq(UserInfo::getUserId, contactId);
                UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);
                if (userInfo == null) {
                    log.warn("搜索不到该用户: {}", contactId);
                    throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
                }
                // 如果是用户的话填写昵称、性别、地区信息
                contactSearchResultDTO.setNickName(userInfo.getNickName());
                contactSearchResultDTO.setGender(userInfo.getGender());
                contactSearchResultDTO.setAreaName(userInfo.getAreaName());
                break;
            case GROUP:
                QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
                groupInfoQueryWrapper.lambda().eq(GroupInfo::getGroupId, contactId);
                GroupInfo groupInfo = groupInfoMapper.selectOne(groupInfoQueryWrapper);
                if (groupInfo == null) {
                    log.warn("搜索不到该群组: {}", contactId);
                    throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
                }
                // 如果是群组的话填写名称为群名
                contactSearchResultDTO.setNickName(groupInfo.getGroupName());
                break;
            default:
                log.warn("进入非USER/GROUP的default分支");
                throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        // 自己查自己的话就直接返回
        if (Objects.equals(UserIdContext.getCurrentUserId(), contactId)) {
            log.info("搜索自己: {}", contactSearchResultDTO);
            return contactSearchResultDTO;
        }
        // 否则补充此联系人和自己的关系（是不是朋友，有没有拉黑状态等等）
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.lambda()
                .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())
                .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(userContactQueryWrapper);
        contactSearchResultDTO.setStatus(userContact == null ? UserContactStatusEnum.NOT_FRIENDS : userContact.getStatus());
        log.info("搜索结果: {}", contactSearchResultDTO);
        return contactSearchResultDTO;
    }
}
