package com.r.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.*;
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
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final RedisUtils redisUtils;

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
                    log.warn("搜索不到该用户 userId: {}", contactId);
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
                    log.warn("搜索不到该群聊 groupId: {}", contactId);
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
            log.info("搜索自己 {}", contactSearchResultDTO);
            return contactSearchResultDTO;
        }
        // 否则补充此联系人和自己的关系（是不是朋友，有没有拉黑状态等等）
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.lambda()
                .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())
                .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(userContactQueryWrapper);
        contactSearchResultDTO.setStatus(userContact == null ? UserContactStatusEnum.NOT_FRIENDS : userContact.getStatus());
        log.info("搜索结果 {}", contactSearchResultDTO);
        return contactSearchResultDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            // 添加联系人
            ContactApplyAddDTO contactApplyAddDTO = new ContactApplyAddDTO();
            contactApplyAddDTO.setApplyUserId(UserIdContext.getCurrentUserId());
            contactApplyAddDTO.setContactId(contactId);
            contactApplyAddDTO.setReceiveUserId(receiveUserId);
            contactApplyAddDTO.setContactType(prefix.getUserContactTypeEnum());
            addContact(contactApplyAddDTO);
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
    public List<BasicInfoDTO> loadContact(UserContactTypeEnum contactType) {
        if (contactType == null) {
            log.warn("查询失败: 传入的联系人类型为null");
            throw new EnumIsNullException(Constants.MESSAGE_STATUS_ERROR);
        }
        // 因为还要查出用户名/群聊名，所以需要联查
        // 而如果是用户，则联查用户信息表，群聊则联查群聊信息表
        List<BasicInfoDTO> basicInfoDTOList;
        switch (contactType) {
            case USER:
                basicInfoDTOList = userContactMapper.selectUserFriends(UserIdContext.getCurrentUserId());
                log.info("查询到好友列表 {}", basicInfoDTOList);
                break;
            case GROUP:
                basicInfoDTOList = userContactMapper.selectGroupFriends(UserIdContext.getCurrentUserId());
                log.info("查询到加入的群聊 {}", basicInfoDTOList);
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        // 设置联系人类型
        basicInfoDTOList.forEach(basicInfoDTO -> {
            basicInfoDTO.setContactType(contactType);
        });
        return basicInfoDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact(ContactApplyAddDTO contactApplyAddDTO) {
        // 如果是加群聊，要判断群聊人数是不是达到了上限
        if (UserContactTypeEnum.GROUP.equals(contactApplyAddDTO.getContactType())) {
            // 查询该群聊的人数
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(UserContact::getContactId, contactApplyAddDTO.getContactId())  // 联系人是群聊
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIENDS);  // 是好友
            Long count = userContactMapper.selectCount(queryWrapper);
            if (count >= redisUtils.getSysSetting().getMaxGroupMemberCount()) {
                log.warn("群聊人数达到上限, 无法加入 count: {}", count);
                throw new GroupMemberCountLimitException(String.format(Constants.MESSAGE_GROUP_MEMBER_COUNT_LIMIT, count));
            }
        }
        // 添加相互关系
        saveOrUpdateMutualContact(contactApplyAddDTO.getApplyUserId(), contactApplyAddDTO.getContactId(), UserContactStatusEnum.FRIENDS);
        log.info("添加好友/群聊成功 contactId: {}", contactApplyAddDTO.getContactId());
        // todo 添加缓存，创建会话等
    }

    @Override
    public ContactBasicInfoDTO getContactBasicInfo(String contactId) {
        // 获取用户信息
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.lambda()
                .eq(UserInfo::getUserId, contactId);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);
        // 拷贝信息
        ContactBasicInfoDTO contactBasicInfoDTO = CopyUtils.copyBean(userInfo, ContactBasicInfoDTO.class);
        // 分情况设置状态
        if (canSeeTheFriend(contactId)) {
            contactBasicInfoDTO.setContactStatus(UserContactStatusEnum.FRIENDS);
        } else contactBasicInfoDTO.setContactStatus(UserContactStatusEnum.NOT_FRIENDS);
        log.info("获取名片信息 {}", contactBasicInfoDTO);
        return contactBasicInfoDTO;
    }

    @Override
    public ContactDetailInfoDTO getContactDetailInfo(String contactId) {
        // 是好友才可以查看详情，先判断是不是好友
        if (!canSeeTheFriend(contactId)) {
            log.warn(Constants.MESSAGE_CAN_NOT_SEE_THE_FRIEND, contactId);
            throw new IllegalOperationException(Constants.MESSAGE_ILLEGAL_OPERATION);
        }
        // 获取用户信息
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.lambda()
                .eq(UserInfo::getUserId, contactId);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);
        // 拷贝信息
        ContactDetailInfoDTO contactDetailInfoDTO = CopyUtils.copyBean(userInfo, ContactDetailInfoDTO.class);
        log.info("获取用户详细信息 {}", contactDetailInfoDTO);
        return contactDetailInfoDTO;
    }

    @Override
    public void removeContact(String contactId, UserContactStatusEnum status) {
        // 合法验证
        if (!canSeeTheFriend(contactId)) {
            log.warn(Constants.MESSAGE_CAN_NOT_SEE_THE_FRIEND, contactId);
            throw new IllegalOperationException(Constants.MESSAGE_ILLEGAL_OPERATION);
        }
        // 更新二者的关系
        saveOrUpdateMutualContact(UserIdContext.getCurrentUserId(), contactId, status);
        // todo 从自己的好友列表缓存中删除联系人
        if (UserContactStatusEnum.DELETED_THE_FRIEND.equals(status)) {
            log.info("成功删除联系人 contactId: {}", contactId);
        } else {
            log.info("成功拉黑联系人 contactId: {}", contactId);
        }
    }

    /**
     * 为二者添加相互的联系人关系
     */
    @Override
    public void saveOrUpdateMutualContact(String fromId, String toId, UserContactStatusEnum status) {
        // 查看contactType
        UserContactTypeEnum c1 = Objects.requireNonNull(IdPrefixEnum.getByPrefix(fromId.charAt(0))).getUserContactTypeEnum();
        UserContactTypeEnum c2 = Objects.requireNonNull(IdPrefixEnum.getByPrefix(toId.charAt(0))).getUserContactTypeEnum();
        UserContactTypeEnum contactType;
        if (UserContactTypeEnum.GROUP.equals(c1) || UserContactTypeEnum.GROUP.equals(c2)) {
            // 两个id里有一个群聊就是群聊关系
            contactType = UserContactTypeEnum.GROUP;
        }
        else {
            // 否则是用户关系
            contactType = UserContactTypeEnum.USER;
        }
        // 获取相对关系
        UserContactStatusEnum anotherStatus;
        log.info("新增/修改相互的联系人关系 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId,contactType, status);
        switch (status) {
            case FRIENDS:
                anotherStatus = UserContactStatusEnum.FRIENDS;
                break;
            case NOT_FRIENDS:
                anotherStatus = UserContactStatusEnum.NOT_FRIENDS;
                break;
            case BLOCKED_THE_FRIEND:
                anotherStatus = UserContactStatusEnum.BLOCKED_BY_FRIEND;
                break;
            case DELETED_THE_FRIEND:
                anotherStatus = UserContactStatusEnum.DELETED_BY_FRIEND;
                break;
            case BLOCKED_BY_FRIEND:
                anotherStatus = UserContactStatusEnum.BLOCKED_THE_FRIEND;
                break;
            case DELETED_BY_FRIEND:
                anotherStatus = UserContactStatusEnum.DELETED_THE_FRIEND;
                break;
            default:
                log.warn("传递了错误的状态 {}", status);
                throw new EnumIsNullException(Constants.MESSAGE_STATUS_ERROR);
        }
        LocalDateTime now = LocalDateTime.now();
        // 查看是否存在关系
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserContact::getUserId, fromId)
                .eq(UserContact::getContactId, toId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        // 除了群聊的好友关系只用写好友和群聊的关系以外，其他都是相互的关系，先把这个条件提取出来写了
        boolean needAnotherContact = !(UserContactTypeEnum.GROUP.equals(contactType) && UserContactStatusEnum.FRIENDS.equals(status));
        if (userContact == null) {
            // 新增关系
            // 对联系人的关系
            UserContact newUserContact = new UserContact();
            newUserContact.setUserId(fromId);
            newUserContact.setContactId(toId);
            newUserContact.setContactType(contactType);
            newUserContact.setStatus(status);
            newUserContact.setCreateTime(now);
            newUserContact.setLastUpdateTime(now);
            userContactMapper.insert(newUserContact);
            // 联系人对自己的关系
            if (needAnotherContact) {
                newUserContact.setId(null);  // 这个对象在上面的insert里会回填自增的id，复用需要删掉这个id，否则插不进去
                newUserContact.setUserId(toId);
                newUserContact.setContactId(fromId);
                newUserContact.setStatus(anotherStatus);
                userContactMapper.insert(newUserContact);
            }
            log.info("新增相互的联系人关系成功 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId,contactType, status);
        } else {
            // 修改关系
            // 对联系人的关系
            UpdateWrapper<UserContact> toFriend = new UpdateWrapper<>();
            toFriend.lambda()
                    .eq(UserContact::getUserId, fromId)
                    .eq(UserContact::getContactId, toId)
                    .set(UserContact::getStatus, status)
                    .set(UserContact::getLastUpdateTime, now);
            update(toFriend);
            // 联系人对自己的关系
            if (needAnotherContact) {
                UpdateWrapper<UserContact> fromFriend = new UpdateWrapper<>();
                fromFriend.lambda()
                        .eq(UserContact::getUserId, toId)
                        .eq(UserContact::getContactId, fromId)
                        .set(UserContact::getStatus, anotherStatus)
                        .set(UserContact::getLastUpdateTime, now);
                update(fromFriend);
            }
            log.info("修改相互的联系人关系成功 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId,contactType, status);
        }
    }

    /**
     * 判断本用户是否能够看到contactId这个用户，即判断对该用户的关系是否是 好友/被删除/被拉黑 中的一种
     */
    private boolean canSeeTheFriend(String contactId) {
        // 查看该用户与本人的关系
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.lambda()
                .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())
                .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(userContactQueryWrapper);
        // 可以显示为好友的状态：好友、被删除、被拉黑
        List<UserContactStatusEnum> friendStatus = new ArrayList<>();
        friendStatus.add(UserContactStatusEnum.FRIENDS);
        friendStatus.add(UserContactStatusEnum.DELETED_BY_FRIEND);
        friendStatus.add(UserContactStatusEnum.BLOCKED_BY_FRIEND);
        return userContact != null && CollUtil.contains(friendStatus, userContact.getStatus());
    }
}
