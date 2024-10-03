package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.ApplyDTO;
import com.r.chat.entity.dto.ContactSearchResultDTO;
import com.r.chat.entity.dto.BasicInfoDTO;
import com.r.chat.entity.enums.*;
import com.r.chat.entity.po.GroupInfo;
import com.r.chat.entity.po.UserContact;
import com.r.chat.entity.po.UserContactApply;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.exception.*;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactApplyMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final UserContactApplyMapper userContactApplyMapper;

    @Override
    public List<BasicInfoDTO> getGroupMemberInfo(String groupId) {
        return userContactMapper.selectGroupMemberByGroupId(groupId);
    }

    @Override
    public ContactSearchResultDTO search(String contactId) {
        ContactSearchResultDTO contactSearchResultDTO = new ContactSearchResultDTO();
        // 先根据传入的id前缀判断是打算加用户还是加群聊
        IdPrefixEnum prefix = IdPrefixEnum.getByPrefix(contactId.charAt(0));
        if (prefix == null) {
            log.warn("搜索id前缀错误");
            return null;  // return null后前端会处理显示无结果
        }
        contactSearchResultDTO.setContactType(prefix.getUserContactTypeEnum());
        contactSearchResultDTO.setContactId(contactId);
        switch (prefix) {
            case USER:
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.lambda().eq(UserInfo::getUserId, contactId);
                UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);
                if (userInfo == null) {
                    log.warn("搜索不到该用户");
                    return null;  // return null后前端会处理显示无结果
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
                    log.warn("搜索不到该群聊");
                    return null;  // return null后前端会处理显示无结果
                }
                // 如果是群聊的话填写名称为群名
                contactSearchResultDTO.setNickName(groupInfo.getGroupName());
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                return null;  // return null后前端会处理显示无结果
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

    @Override
    public JoinTypeEnum applyAdd(ApplyDTO applyDTO) {
        String contactId = applyDTO.getContactId();
        // 查看对方是否已将自己拉黑
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.lambda()
                .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())
                .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(userContactQueryWrapper);
        if (userContact != null && UserContactStatusEnum.BLOCKED_BY_FRIEND.equals(userContact.getStatus())) {
            log.warn("添加联系人失败: 已被拉黑");
            throw new BeingBlockedException(Constants.MESSAGE_BING_BLOCKED);
        }
        // 根据联系人类型处理请求
        IdPrefixEnum prefix = IdPrefixEnum.getByPrefix(contactId.charAt(0));
        if (prefix == null) {
            log.warn("联系人id前缀错误");
            // 默认提示用户不存在
            throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        // 查看想添加的联系人是否存在，存在则获取他被别人添加时的添加方式，同时获取要发送申请时接收人的id
        JoinTypeEnum joinType;
        String receiveUserId;
        switch (prefix) {
            case USER:
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.lambda()
                        .eq(UserInfo::getUserId, contactId);
                UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);
                if (userInfo == null) {
                    log.warn("添加联系人失败: 用户不存在");
                    throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
                }
                joinType = userInfo.getJoinType();
                receiveUserId = userInfo.getUserId();
                break;
            case GROUP:
                QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
                groupInfoQueryWrapper.lambda()
                        .eq(GroupInfo::getGroupId, contactId);
                GroupInfo groupInfo = groupInfoMapper.selectOne(groupInfoQueryWrapper);
                if (groupInfo == null) {
                    log.warn("添加联系人失败: 群聊不存在");
                    throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
                }
                // 群存在但是解散了
                if (GroupInfoStatusEnum.DISBAND.equals(groupInfo.getStatus())) {
                    log.warn("添加联系人失败: 群聊已解散");
                    throw new GroupDisbandException(Constants.MESSAGE_GROUP_ALREADY_DISBAND);
                }
                joinType = groupInfo.getJoinType();
                // 接收人是群主
                receiveUserId = groupInfo.getGroupOwnerId();
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        // 如果可以直接添加，则直接添加，不用发送申请
        if (JoinTypeEnum.JOIN_DIRECTLY.equals(joinType)) {
            // todo 添加联系人
            log.info("可直接添加联系人, 已直接添加联系人");
            return joinType;
        }
        // 添加申请
        // 先看之前是不是申请过
        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
        userContactApplyQueryWrapper.lambda()
                .eq(UserContactApply::getApplyUserId, UserIdContext.getCurrentUserId())
                .eq(UserContactApply::getContactId, contactId)
                .eq(UserContactApply::getReceiveUserId, receiveUserId);
        UserContactApply userContactApply = userContactApplyMapper.selectOne(userContactApplyQueryWrapper);
        LocalDateTime now = LocalDateTime.now();
        if (userContactApply == null) {
            // 没申请过，添加申请
            log.info("未申请添加过该联系人, 添加申请数据");
            UserContactApply uca = new UserContactApply();
            uca.setApplyUserId(UserIdContext.getCurrentUserId());
            uca.setContactId(contactId);
            uca.setReceiveUserId(receiveUserId);
            uca.setApplyInfo(applyDTO.getApplyInfo());
            uca.setContactType(prefix.getUserContactTypeEnum());
            uca.setLastApplyTime(now);
            uca.setStatus(UserContactApplyStatusEnum.PENDING);
            userContactApplyMapper.insert(uca);
        } else {
            // 申请过，更新申请时间、申请信息，重设申请状态为待处理
            log.info("已申请添加过该联系人, 重新设置申请状态未待处理");
            userContactApply.setStatus(UserContactApplyStatusEnum.PENDING);
            userContactApply.setApplyInfo(applyDTO.getApplyInfo());
            userContactApply.setLastApplyTime(now);
            userContactApplyMapper.updateById(userContactApply);
        }
        // 发送ws消息通知接收者
        // 如果原本就处于待处理，就不管，不然会多次发送消息给接收方，这不合理
        if (userContactApply != null && UserContactApplyStatusEnum.PENDING.equals(userContactApply.getStatus())) {
            // todo 发送ws消息
            log.info("发送ws消息通知接收者 receiveUserId: {}", receiveUserId);
        }
        return joinType;
    }

    @Override
    public List<BasicInfoDTO> loadContact(UserContactTypeEnum userContactType) {
        if (userContactType == null) {
            log.warn("查询失败: 传入的联系人类型为null");
            throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        // 因为还要查出用户名/群聊名，所以需要联查
        // 而如果是用户，则联查用户信息表，群聊则联查群聊信息表
        List<BasicInfoDTO> basicInfoDTOList;
        switch (userContactType) {
            case FRIENDS:
                basicInfoDTOList = userContactMapper.selectUserFriends(UserIdContext.getCurrentUserId());
                log.info("查询到好友列表: {}", basicInfoDTOList);
                break;
            case GROUP:
                basicInfoDTOList = userContactMapper.selectGroupFriends(UserIdContext.getCurrentUserId());
                log.info("查询到加入的群聊: {}", basicInfoDTOList);
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        return basicInfoDTOList;
    }
}
