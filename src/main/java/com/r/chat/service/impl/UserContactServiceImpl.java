package com.r.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.*;
import com.r.chat.entity.enums.*;
import com.r.chat.entity.message.ContactApplyNotice;
import com.r.chat.entity.message.GroupAddAcceptedNotice;
import com.r.chat.entity.message.UserAddAcceptNotice;
import com.r.chat.entity.message.UserAddByOthersNotice;
import com.r.chat.entity.po.*;
import com.r.chat.entity.vo.ChatSessionUserVO;
import com.r.chat.exception.*;
import com.r.chat.mapper.*;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
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
    private final ChatSessionServiceImpl chatSessionServiceImpl;
    private final ChatSessionUserServiceImpl chatSessionUserServiceImpl;

    private final UserContactMapper userContactMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserContactApplyMapper userContactApplyMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final ChatMessageMapper chatMessageMapper;

    private final RedisUtils redisUtils;
    private final ChannelUtils channelUtils;

    @Override
    public List<BasicInfoDTO> getGroupMemberInfo(String groupId) {
        return userContactMapper.selectGroupMemberByGroupId(groupId);
    }

    @Override
    public ContactSearchResultDTO search(String contactId) {
        ContactSearchResultDTO contactSearchResultDTO = new ContactSearchResultDTO();
        // 先根据传入的id前缀判断是打算加用户还是加群聊
        IdPrefixEnum prefix = IdPrefixEnum.getPrefix(contactId);
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
                throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
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
        IdPrefixEnum prefix = IdPrefixEnum.getPrefix(contactId);
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
                throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
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
        Long now = System.currentTimeMillis();
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
            // 发送通知，让前端渲染出有新的申请（红点）和申请信息
            ContactApplyNotice message = new ContactApplyNotice();
            message.setReceiveId(receiveUserId);
            message.setUserContactApply(uca);
            channelUtils.sendNotice(message);
            log.info("发送有新的申请信息的通知 {}", message);
        } else {
            // 申请过，更新申请时间、申请信息，重设申请状态为待处理
            // 先保存原来的申请状态
            UserContactApplyStatusEnum originStatus = userContactApply.getStatus();
            log.info("已申请添加过该联系人, 重新设置申请状态未待处理");
            userContactApply.setStatus(UserContactApplyStatusEnum.PENDING);
            userContactApply.setApplyInfo(applyDTO.getApplyInfo());
            userContactApply.setLastApplyTime(now);
            userContactApplyMapper.updateById(userContactApply);
            // 只有申请被处理过才发送ws通知
            // 如果原本就处于待处理，就不管，不然会多次发送通知给接收方，这不合理
            if (UserContactApplyStatusEnum.PENDING.equals(originStatus)) {
                ContactApplyNotice message = new ContactApplyNotice();
                message.setReceiveId(receiveUserId);
                message.setUserContactApply(userContactApply);
                channelUtils.sendNotice(message);
                log.info("重新发送有新的申请信息的通知 {}", message);
            }
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
                throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
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
        log.info("新增好友/群聊的好友关系成功 contactId: {}", contactApplyAddDTO.getContactId());

        // 登录的时候把联系人id列表放在了redis上了，现在添加了新朋友/新群聊，需要更新缓存
        // 判断是加了新朋友还是加入了新群聊
        if (UserContactTypeEnum.USER.equals(contactApplyAddDTO.getContactType())) {
            // 加了新朋友的话需要更新两个人的缓存
            redisUtils.addToContactIds(contactApplyAddDTO.getContactId(), contactApplyAddDTO.getApplyUserId());
            log.info("更新redis用户联系人id列表 userId: {}, addId: {}", contactApplyAddDTO.getContactId(), contactApplyAddDTO.getApplyUserId());
        }
        // 加了新群聊则只需要更新自己的联系人id列表
        redisUtils.addToContactIds(contactApplyAddDTO.getApplyUserId(), contactApplyAddDTO.getContactId());
        log.info("更新redis用户联系人id列表 userId: {}, addId: {}", contactApplyAddDTO.getApplyUserId(), contactApplyAddDTO.getContactId());

        // 获取申请信息，添加好友后要发送这个申请信息
        QueryWrapper<UserContactApply> ucaQueryWrapper = new QueryWrapper<>();
        ucaQueryWrapper.lambda()
                .eq(UserContactApply::getApplyUserId, contactApplyAddDTO.getApplyUserId())
                .eq(UserContactApply::getContactId, contactApplyAddDTO.getContactId());
        UserContactApply userContactApply = userContactApplyMapper.selectOne(ucaQueryWrapper);

        // 创建会话（添加好友/群聊会生成聊天框，好友聊天框里申请人会发送申请信息的内容）
        String sessionId;
        Long now = System.currentTimeMillis();
        // 好友sessionId是两个人的id生成的，群聊sessionId只用群聊id即可
        if (UserContactTypeEnum.USER.equals(contactApplyAddDTO.getContactType())) {
            sessionId = StringUtils.getSessionId(new String[]{contactApplyAddDTO.getApplyUserId(), contactApplyAddDTO.getContactId()});
            // 创建/更新会话
            // 因为两个人的会话是唯一的，就算删了好友加回来也是同一个会话，所以可能是新增也可能是更新
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(userContactApply.getApplyInfo());
            chatSession.setLastReceiveTime(now);
            chatSessionServiceImpl.saveOrUpdate(chatSession);
            log.info("新增/更新用户间会话 {}", chatSession);

            // 创建用户到会话的关系，同样可能是新增或更新
            // 申请方对被申请方的
            // 获取会话对方的名称，即被申请方的名字
            QueryWrapper<UserInfo> uiQueryWrapper = new QueryWrapper<>();
            uiQueryWrapper.lambda().eq(UserInfo::getUserId, contactApplyAddDTO.getContactId());
            UserInfo contactUserInfo = userInfoMapper.selectOne(uiQueryWrapper);
            // 构造用户会话关系
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(contactApplyAddDTO.getApplyUserId());
            chatSessionUser.setContactId(contactApplyAddDTO.getContactId());
            chatSessionUser.setContactName(contactUserInfo.getNickName());
            chatSessionUserServiceImpl.saveOrUpdate(chatSessionUser);
            log.info("新增/修改申请方对被申请方的用户会话关系 {}", chatSessionUser);

            // 被申请方对申请方的
            // 获取会话对方的名称，即申请方的名字
            uiQueryWrapper = new QueryWrapper<>();
            uiQueryWrapper.lambda().eq(UserInfo::getUserId, contactApplyAddDTO.getApplyUserId());
            UserInfo applyUserInfo = userInfoMapper.selectOne(uiQueryWrapper);
            // 构造用户会话关系
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(contactApplyAddDTO.getContactId());
            chatSessionUser.setContactId(contactApplyAddDTO.getApplyUserId());
            chatSessionUser.setContactName(applyUserInfo.getNickName());
            chatSessionUserServiceImpl.saveOrUpdate(chatSessionUser);
            log.info("新增/修改被申请方对申请方的用户会话关系 {}", chatSessionUser);

            // 新增这个聊天信息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageContent(userContactApply.getApplyInfo());
            chatMessage.setSendUserId(contactApplyAddDTO.getApplyUserId());
            chatMessage.setContactId(contactApplyAddDTO.getContactId());
            chatMessage.setMessageType(MessageTypeEnum.CHAT);
            chatMessage.setSendUserNickName(applyUserInfo.getNickName());
            chatMessage.setSendTime(now);
            chatMessage.setContactType(contactApplyAddDTO.getContactType());
            chatMessage.setStatus(MessageStatusEnum.SENT);
            chatMessageMapper.insert(chatMessage);
            log.info("新增聊天信息 {}", chatMessage);

            // 给申请人和被申请人都发送通知，带上会话信息，让前端渲染出会话
            // 给被申请人发送同意了别人申请的通知
            UserAddAcceptNotice userAddAcceptMessage = new UserAddAcceptNotice();
            userAddAcceptMessage.setReceiveId(contactApplyAddDTO.getContactId());
            // 构造前端需要的会话信息
            ChatSessionUserVO chatSessionUserVO = new ChatSessionUserVO();
            chatSessionUserVO.setUserId(contactApplyAddDTO.getApplyUserId());
            chatSessionUserVO.setContactId(contactApplyAddDTO.getContactId());
            chatSessionUserVO.setContactName(contactUserInfo.getNickName());
            chatSessionUserVO.setContactType(contactApplyAddDTO.getContactType());
            chatSessionUserVO.setSessionId(sessionId);
            chatSessionUserVO.setLastMessage(userContactApply.getApplyInfo());
            chatSessionUserVO.setLastReceiveTime(now);
            userAddAcceptMessage.setChatSessionUserVO(chatSessionUserVO);
            channelUtils.sendNotice(userAddAcceptMessage);
            log.info("发送被申请人同意了别人申请的ws通知  {}", userAddAcceptMessage);

            // 给申请人发送申请被别人同意的通知
            UserAddByOthersNotice userAddByOthersNotice = new UserAddByOthersNotice();
            userAddByOthersNotice.setReceiveId(contactApplyAddDTO.getApplyUserId());
            // 更换下会话框显示内容
            chatSessionUserVO.setUserId(contactApplyAddDTO.getContactId());
            chatSessionUserVO.setContactId(contactApplyAddDTO.getApplyUserId());
            chatSessionUserVO.setContactName(applyUserInfo.getNickName());
            userAddByOthersNotice.setChatSessionUserVO(chatSessionUserVO);
            channelUtils.sendNotice(userAddByOthersNotice);
            log.info("发送申请人申请被别人同意的ws通知  {}", userAddByOthersNotice);

            log.info("添加好友成功 {}", contactApplyAddDTO);
        } else {
            sessionId = StringUtils.getSessionId(new String[]{contactApplyAddDTO.getContactId()});

            // 查找申请人信息，获取名字，在加入群聊时展示给其他人
            UserInfo applyUserInfo = userInfoMapper.selectById(contactApplyAddDTO.getApplyUserId());
            // 加入群聊的信息
            String joinMessage = String.format(Constants.MESSAGE_JOIN_GROUP, applyUserInfo.getNickName());

            // 创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(joinMessage);
            chatSession.setLastReceiveTime(now);
            chatSessionServiceImpl.saveOrUpdate(chatSession);
            log.info("新增/更新用户与群聊之间的会话 {}", chatSession);

            // 创建会话关系
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(contactApplyAddDTO.getApplyUserId());
            chatSessionUser.setContactId(contactApplyAddDTO.getContactId());
            // 查询群聊名
            GroupInfo groupInfo = groupInfoMapper.selectById(contactApplyAddDTO.getContactId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUserServiceImpl.saveOrUpdate(chatSessionUser);
            log.info("新增/更新用户对群聊的会话关系 {}", chatSessionUser);

            // 新增聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setContactId(contactApplyAddDTO.getContactId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP);
            chatMessage.setMessageContent(joinMessage);
            chatMessage.setMessageType(MessageTypeEnum.NOTICE);
            chatMessage.setSendTime(now);
            chatMessage.setStatus(MessageStatusEnum.SENT);
            chatMessageMapper.insert(chatMessage);
            log.info("新增聊天消息 {}", chatMessage);

            // 将群聊添加到申请人的联系人id列表中
            redisUtils.addToContactIds(contactApplyAddDTO.getApplyUserId(), contactApplyAddDTO.getContactId());
            log.info("更新redis用户 {} 的联系人id列表 添加群聊id: {}", contactApplyAddDTO.getApplyUserId(), groupInfo.getGroupId());

            // 将申请人加入群聊的channelGroup中
            channelUtils.addUser2Group(contactApplyAddDTO.getApplyUserId(), groupInfo.getGroupId());
            log.info("将申请人 {} 加入到群聊的channelGroup中 groupId: {}", contactApplyAddDTO.getApplyUserId(), groupInfo.getGroupId());

            // 发送ws通知前端渲染加入的群聊的会话
            GroupAddAcceptedNotice groupAddAcceptedNotice = new GroupAddAcceptedNotice();
            // 构建申请人看到的群聊会话数据，让前端渲染
            ChatSessionUserVO chatSessionUserVO = CopyUtils.copyBean(chatSessionUser, ChatSessionUserVO.class);
            chatSessionUserVO.setContactType(UserContactTypeEnum.GROUP);
            chatSessionUserVO.setLastMessage(joinMessage);
            chatSessionUserVO.setLastReceiveTime(now);
            // 查询群聊人数
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(UserContact::getContactId, contactApplyAddDTO.getContactId())
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIENDS);
            Long count = userContactMapper.selectCount(queryWrapper);
            chatSessionUserVO.setMemberCount(count);
            groupAddAcceptedNotice.setChatSessionUserVO(chatSessionUserVO);
            groupAddAcceptedNotice.setReceiveId(contactApplyAddDTO.getApplyUserId());  // 发送给申请人
            channelUtils.sendNotice(groupAddAcceptedNotice);
            log.info("发送群聊加入申请被通过的ws通知  {}", groupAddAcceptedNotice);

            log.info("添加群聊成功  {}", contactApplyAddDTO);
        }
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
        UserContactTypeEnum c1 = Objects.requireNonNull(IdPrefixEnum.getPrefix(fromId)).getUserContactTypeEnum();
        UserContactTypeEnum c2 = Objects.requireNonNull(IdPrefixEnum.getPrefix(toId)).getUserContactTypeEnum();
        UserContactTypeEnum contactType;
        if (UserContactTypeEnum.GROUP.equals(c1) || UserContactTypeEnum.GROUP.equals(c2)) {
            // 两个id里有一个群聊就是群聊关系
            contactType = UserContactTypeEnum.GROUP;
        } else {
            // 否则是用户关系
            contactType = UserContactTypeEnum.USER;
        }
        // 获取相对关系
        UserContactStatusEnum anotherStatus;
        log.info("新增/修改相互的联系人关系 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId, contactType, status);
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
            log.info("新增相互的联系人关系成功 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId, contactType, status);
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
            log.info("修改相互的联系人关系成功 fromId: {}, toId: {}, contactType: {} status: {}", fromId, toId, contactType, status);
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
