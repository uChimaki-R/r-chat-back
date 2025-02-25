package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.context.AdminContext;
import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.*;
import com.r.chat.entity.enums.*;
import com.r.chat.entity.notice.GroupCreatedNotice;
import com.r.chat.entity.notice.ContactRenameNotice;
import com.r.chat.entity.notice.GroupDisbandNotice;
import com.r.chat.entity.notice.GroupMemberLeaveOrIsRemovedNotice;
import com.r.chat.entity.po.*;
import com.r.chat.entity.vo.ChatDataVO;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.exception.*;
import com.r.chat.mapper.ChatMessageMapper;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IChatSessionService;
import com.r.chat.service.IChatSessionUserService;
import com.r.chat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 群聊信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Service
@Slf4j
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IGroupInfoService {
    @Resource
    private IChatSessionService chatSessionServiceImpl;
    @Resource
    private IChatSessionUserService chatSessionUserServiceImpl;
    @Resource
    private IUserContactService userContactServiceImpl;
    @Lazy  // 注入自己，循环依赖使用@Lazy解决
    @Resource
    private IGroupInfoService groupInfoServiceImpl;

    @Resource
    private UserContactMapper userContactMapper;
    @Resource
    private GroupInfoMapper groupInfoMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ChannelUtils channelUtils;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateGroupInfo(GroupInfoDTO groupInfoDTO) {
        LocalDateTime now = LocalDateTime.now();  // 当前时间
        // 填充GroupInfo对象
        GroupInfo groupInfo = CopyUtils.copyBean(groupInfoDTO, GroupInfo.class);
        if (StringUtils.isEmpty(groupInfoDTO.getGroupId())) {
            // 新增群聊
            // 判断该用户的群聊是否已经达到上限
            SysSettingDTO sysSettingDTO = redisUtils.getSysSetting();
            Long count = lambdaQuery().eq(GroupInfo::getGroupOwnerId, UserTokenInfoContext.getCurrentUserId()).count();
            if (count > sysSettingDTO.getMaxGroupCount()) {
                log.warn("拒绝新增群聊: 群聊数量达到上限 [{}]", sysSettingDTO.getMaxGroupCount());
                throw new GroupCountLimitException(String.format(Constants.MESSAGE_GROUP_COUNT_LIMIT, sysSettingDTO.getMaxGroupCount()));
            }

            // 添加群聊到数据库
            // 新建群号，补充内容
            groupInfo.setGroupId(StringUtils.getRandomGroupId());
            groupInfo.setGroupOwnerId(UserTokenInfoContext.getCurrentUserId());
            groupInfo.setCreateTime(now);
            groupInfo.setStatus(GroupInfoStatusEnum.NORMAL);
            save(groupInfo);
            log.info("新增群聊信息 {}", groupInfo);

            // 将自己加入群聊（添加联系人信息）
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setContactType(UserContactTypeEnum.GROUP);
            userContact.setStatus(UserContactStatusEnum.FRIENDS);
            userContact.setCreateTime(now);
            userContact.setLastUpdateTime(now);
            userContactMapper.insert(userContact);
            log.info("将自己添加入创建的群聊中 {}", userContact);

            Long millis = System.currentTimeMillis();

            // 新增群聊会话，会话是唯一的，可能新增可能更新
            String sessionId = StringUtils.getSessionId(new String[]{groupInfo.getGroupId()});
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(Constants.MESSAGE_GROUP_CREATED);
            chatSession.setLastReceiveTime(millis);
            chatSessionServiceImpl.saveOrUpdate(chatSession);
            log.info("新增/更新群聊会话 {}", chatSession);

            // 新增群主对群聊的会话关系
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(UserTokenInfoContext.getCurrentUserId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUserServiceImpl.saveOrUpdateByMultiId(chatSessionUser);
            log.info("新增/更新用户会话关系 {}", chatSessionUser);

            // 新增群聊创建时的提示消息，提示消息没有发送人
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.NOTICE);
            chatMessage.setMessageContent(Constants.MESSAGE_GROUP_CREATED);
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP);
            chatMessage.setSendStatus(MessageStatusEnum.SENT);
            chatMessage.setSendTime(millis);
            chatMessageMapper.insert(chatMessage);
            log.info("新增群聊创建成功提示信息 {}", chatMessage);

            // 更新自己的联系人id列表
            redisUtils.addToContactIds(UserTokenInfoContext.getCurrentUserId(), groupInfo.getGroupId());
            log.info("更新redis用户联系人id列表 添加群聊id: {}", groupInfo.getGroupId());

            // 将自己加入群聊的channelGroup中
            channelUtils.addUser2Group(UserTokenInfoContext.getCurrentUserId(), groupInfo.getGroupId());
            log.info("将自己加入到群聊的channelGroup中 groupId: {}", groupInfo.getGroupId());

            // 发送ws通知前端渲染群聊会话
            GroupCreatedNotice groupCreatedNotice = new GroupCreatedNotice();
            groupCreatedNotice.setReceiveId(UserTokenInfoContext.getCurrentUserId());
            // 构建用于渲染会话的数据
            ChatDataVO chatDataVO = ChatDataVO.fromChatData(chatMessage, chatSession, groupInfo.getGroupId(), groupInfo.getGroupName());
            chatDataVO.setMemberCount(1L);  // 初始只有自己一个人
            groupCreatedNotice.setChatDataVO(chatDataVO);
            channelUtils.sendNotice(groupCreatedNotice);
            log.info("发送群聊创建成功的ws通知 {}", groupCreatedNotice);

            log.info("新增群聊成功 {}", groupInfo);
        } else {
            // 查询原来的信息
            GroupInfo dbInfo = groupInfoMapper.selectById(groupInfo.getGroupId());
            if (dbInfo == null) {
                // 群聊不存在
                throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
            }
            // 修改群聊信息
            updateById(groupInfo);
            log.info("修改群聊信息 {}", groupInfo);
            // 如果群聊名改了的话需要把会话信息中的联系名称也改了，并且发送通知给前端让前端重新渲染
            if (!dbInfo.getGroupName().equals(groupInfo.getGroupName())) {
                log.info("修改了群聊名称, 需要修改会话中的群聊名称");
                UpdateWrapper<ChatSessionUser> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda()
                        .eq(ChatSessionUser::getContactId, groupInfo.getGroupId())
                        .set(ChatSessionUser::getContactName, groupInfo.getGroupName());
                chatSessionUserServiceImpl.update(updateWrapper);
                log.info("更新会话中的群聊名称信息成功");
                // 发送群聊名修改的通知给所有群成员
                ContactRenameNotice contactRenameNotice = new ContactRenameNotice();
                contactRenameNotice.setReceiveId(groupInfo.getGroupId());
                contactRenameNotice.setContactId(groupInfo.getGroupId());
                contactRenameNotice.setContactName(groupInfo.getGroupName());
                channelUtils.sendNotice(contactRenameNotice);
                log.info("发送群聊名称修改的ws通知 {}", contactRenameNotice);
            }

            log.info("修改群聊信息成功 {}", groupInfoDTO);
        }
        // 头像保存到本地
        // 新建的群聊不会传groupId，需要手动设置一下
        groupInfoDTO.setGroupId(groupInfo.getGroupId());
        FileUtils.saveAvatarFile(groupInfoDTO);
    }

    @Override
    public Page<GroupDetailInfoVO> loadGroupInfo4Admin(Page<GroupDetailInfoVO> page, GroupInfoQueryDTO groupInfoQueryDTO) {
        // 前端发送的id是不带前缀的，如果传递了id的查询条件的话需要补充前缀
        if (!StringUtils.isEmpty(groupInfoQueryDTO.getGroupId())) {
            // 补充groupId的前缀
            groupInfoQueryDTO.setGroupId(IdPrefixEnum.GROUP.getPrefix() + groupInfoQueryDTO.getGroupId());
        }
        if (!StringUtils.isEmpty(groupInfoQueryDTO.getGroupOwnerId())) {
            // 补充群主id的前缀
            groupInfoQueryDTO.setGroupOwnerId(IdPrefixEnum.USER.getPrefix() + groupInfoQueryDTO.getGroupOwnerId());
        }
        return groupInfoMapper.selectGroupDetailInfoPage(page, groupInfoQueryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disbandGroup(String groupId) {
        GroupInfo groupInfo = lambdaQuery().eq(GroupInfo::getGroupId, groupId).one();
        if (groupInfo == null) {
            log.warn("解散群聊失败: 群聊不存在 groupId: {}", groupId);
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        // 有两种情况可以解散群聊：1、群主本人解散 2、管理员强制解散
        if (!groupInfo.getGroupOwnerId().equals(UserTokenInfoContext.getCurrentUserId()) && !AdminContext.isAdmin()) {
            log.warn("解散群聊失败: 非群主或管理员操作 操作人id: {}, groupId: {}, 群主id: {}", UserTokenInfoContext.getCurrentUserId(), groupId, groupInfo.getGroupOwnerId());
            throw new IllegalOperationException(Constants.MESSAGE_NOT_GROUP_OWNER + "或" + Constants.MESSAGE_NOT_ADMIN);
        }
        // 更新群聊状态为解散
        groupInfo.setStatus(GroupInfoStatusEnum.DISBAND);
        updateById(groupInfo);
        log.info("更新群聊状态为已解散 {}", groupInfo);
        // 将所有群成员对群聊的关系设置为被删除
        UpdateWrapper<UserContact> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserContact::getContactId, groupInfo.getGroupId())
                .eq(UserContact::getContactType, UserContactTypeEnum.GROUP)
                .set(UserContact::getStatus, UserContactStatusEnum.DELETED_BY_FRIEND)
                .set(UserContact::getLastUpdateTime, LocalDateTime.now());
        userContactMapper.update(null, updateWrapper);
        log.info("将所有群成员对群聊的关系设置为被删除");

        // 获取所有群成员，从在线的保存了缓存的人的id列表中移除
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserContact::getContactId, groupInfo.getGroupId())
                .eq(UserContact::getContactType, UserContactTypeEnum.GROUP)
                .eq(UserContact::getStatus, UserContactStatusEnum.DELETED_BY_FRIEND);
        List<UserContact> contacts = userContactMapper.selectList(queryWrapper);
        if (contacts == null || contacts.isEmpty()) return;
        for (UserContact contact : contacts) {
            redisUtils.removeFromContactIds(contact.getUserId(), groupId);
        }
        log.info("从缓存的群聊成员的id列表中移除群聊 群成员: {}", contacts);

        // 信息提示是群主解散的还是管理员解散的
        String disbandMessage = AdminContext.isAdmin() ? Constants.MESSAGE_GROUP_DISBAND_BY_ADMIN : Constants.MESSAGE_GROUP_DISBAND_BY_OWNER;

        // 更新会话
        String sessionId = StringUtils.getSessionId(new String[]{groupId});
        Long now = System.currentTimeMillis();
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(disbandMessage);
        chatSession.setLastReceiveTime(now);
        chatSessionServiceImpl.saveOrUpdate(chatSession);
        log.info("更新群聊会话 {}", chatSession);

        // 新增这条解散信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.NOTICE);
        chatMessage.setMessageContent(disbandMessage);
        chatMessage.setSendTime(now);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP);
        chatMessage.setSendStatus(MessageStatusEnum.SENT);
        chatMessageMapper.insert(chatMessage);
        log.info("新增解散群聊的消息 {}", chatMessage);

        // 发送群聊解散通知
        GroupDisbandNotice notice = new GroupDisbandNotice();
        ChatDataVO chatDataVO = ChatDataVO.fromChatData(chatMessage, chatSession, groupId, groupInfo.getGroupName());
        notice.setChatDataVO(chatDataVO);
        notice.setReceiveId(groupId);
        channelUtils.sendNotice(notice);
        log.info("发送群聊解散的ws通知 {}", notice);

        log.info("解散群聊成功 groupId: {}", groupId);
    }

    @Override
    public void addOrRemoveGroupMember(GroupMemberOpDTO opInfo) {
        GroupMemberOpTypeEnum opType = opInfo.getOpType();
        if (opType == null) {
            log.warn("群成员操作类型为空");
            throw new EnumIsNullException(Constants.MESSAGE_ENUM_ERROR);
        }
        String groupId = opInfo.getGroupId();
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null) {
            log.warn("添加或移除群成员失败: 群聊不存在 groupId: {}", groupId);
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        if (!groupInfo.getGroupOwnerId().equals(UserTokenInfoContext.getCurrentUserId())) {
            log.warn("添加或移除群成员失败: 非群主操作 groupOwnerId: {}", groupInfo.getGroupOwnerId());
            throw new IllegalOperationException(Constants.MESSAGE_NOT_GROUP_OWNER);
        }
        String[] contactIds = opInfo.getContactIds().split(",");
        // 依次发送消息
        for (String contactId : contactIds) {
            switch (opType) {
                case ADD:
                    // 添加好友关系
                    ContactApplyAddDTO contactApplyAddDTO = new ContactApplyAddDTO();
                    contactApplyAddDTO.setApplyUserId(contactId);
                    contactApplyAddDTO.setContactId(groupId);
                    contactApplyAddDTO.setContactType(UserContactTypeEnum.GROUP);
                    userContactServiceImpl.addContact(contactApplyAddDTO);
                    break;
                case REMOVE:
                    // 内部调用的方法，spring的@Transactional注解不会生效，需要注入自己，用类来调用
                    groupInfoServiceImpl.leaveGroup(contactId, groupId);
                    break;
                default:
                    log.warn(Constants.IN_SWITCH_DEFAULT);
                    throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String idToLeave, String groupId) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null) {
            log.warn("移除群成员失败: 群聊不存在 groupId: {}", groupId);
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        // 不能移除群主
        if (groupInfo.getGroupOwnerId().equals(idToLeave)) {
            log.warn("移除群成员失败: 不可以移除群主 groupId: {}, idToLeave: {}", groupId, idToLeave);
            throw new IllegalOperationException(Constants.MESSAGE_CANNOT_REMOVE_OWNER);
        }
        // 因为只有用户到群聊的关系，所以直接删除关系即可
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserContact::getUserId, idToLeave)
                .eq(UserContact::getContactId, groupId);
        int count = userContactMapper.delete(queryWrapper);
        if (count == 0) {
            log.warn("没有删除任何用户联系人关系, 意味着用户不在此群聊里");
            throw new IllegalOperationException(Constants.MESSAGE_NOT_IN_THE_GROUP);
        }
        log.info("成功删除用户与群聊的联系人关系 idToLeave: {}, groupId: {}", idToLeave, groupId);

        // 会话操作
        String sessionId = StringUtils.getSessionId(new String[]{groupId});
        Long now = System.currentTimeMillis();
        // 被移除和退出群聊是不一样的消息
        // 如果idToLeave是自己，则是退出群聊的操作，反之则是群主的操作，因为自己如果是群主的话也不允许移除自己，逻辑通
        String lastMessage;
        if (idToLeave.equals(UserTokenInfoContext.getCurrentUserId())) {
            // 自己退出群聊，名字可以从上下文里取
            lastMessage = String.format(Constants.MESSAGE_LEAVE_GROUP, UserTokenInfoContext.getCurrentUserNickName());
        } else {
            // 被群主移出群聊，需要查询被移除的人的名字
            UserInfo userInfo = userInfoMapper.selectById(idToLeave);
            if (userInfo == null) {
                log.warn("移除群成员失败: 该用户不存在 idToLeave: {}", idToLeave);
                throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
            }
            lastMessage = String.format(Constants.MESSAGE_REMOVED_FROM_GROUP, userInfo.getNickName());
        }
        // 更新会话信息
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(lastMessage);
        chatSession.setLastReceiveTime(now);
        chatSessionServiceImpl.saveOrUpdate(chatSession);
        log.info("更新会话信息 {}", chatSession);

        // 添加这条退出群聊的消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.NOTICE);
        chatMessage.setMessageContent(lastMessage);
        chatMessage.setSendTime(now);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP);
        chatMessage.setSendStatus(MessageStatusEnum.SENT);
        chatMessageMapper.insert(chatMessage);
        log.info("新增退出群聊的消息 {}", chatMessage);

        // 更新redis中的联系人id列表
        redisUtils.removeFromContactIds(idToLeave, groupId);

        // 获取最新的群聊人数等信息
        QueryWrapper<UserContact> countWrapper = new QueryWrapper<>();
        countWrapper.lambda()
                .eq(UserContact::getContactId, groupId)
                .eq(UserContact::getStatus, UserContactStatusEnum.FRIENDS);
        Long cnt = userContactMapper.selectCount(countWrapper);

        // 发送ws通知给群聊成员
        GroupMemberLeaveOrIsRemovedNotice notice = new GroupMemberLeaveOrIsRemovedNotice();
        ChatDataVO chatDataVO = ChatDataVO.fromChatData(chatMessage, chatSession, groupId, groupInfo.getGroupName());
        chatDataVO.setMemberCount(cnt);
        notice.setChatDataVO(chatDataVO);
        notice.setReceiveId(groupId);
        notice.setLeaveUserId(idToLeave);
        channelUtils.sendNotice(notice);
        log.info("发送离开群聊或者被移出群聊的ws通知 {}", notice);
    }

    @Override
    public GroupDetailInfoVO getGroupDetailInfo(String groupId) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null) {
            // 群聊不存在
            log.warn("获取群聊基本信息失败: 群聊不存在 groupId: {}", groupId);
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        if (GroupInfoStatusEnum.DISBAND.equals(groupInfo.getStatus())) {
            // 群聊已解散
            log.warn("获取群聊基本信息失败: 群聊已解散 groupId: {}", groupId);
            throw new GroupDisbandException(Constants.MESSAGE_GROUP_ALREADY_DISBAND);
        }
        // 看该用户是否在群里
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserContact::getUserId, UserTokenInfoContext.getCurrentUserId())
                .eq(UserContact::getContactId, groupId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        if (userContact == null || !UserContactStatusEnum.FRIENDS.equals(userContact.getStatus())) {
            // 不在这个群或者被移除群了，属于非法操作
            log.warn("获取群聊基本信息失败: 不在该群聊中 groupId: {}", groupId);
            throw new IllegalOperationException(Constants.MESSAGE_NOT_IN_THE_GROUP);
        }
        GroupDetailInfoVO groupDetailInfoVO = CopyUtils.copyBean(groupInfo, GroupDetailInfoVO.class);
        // 计算群成员数
        QueryWrapper<UserContact> countWrapper = new QueryWrapper<>();
        countWrapper.lambda().eq(UserContact::getContactId, groupDetailInfoVO.getGroupId());
        Long count = userContactMapper.selectCount(countWrapper);
        groupDetailInfoVO.setMemberCount(count);
        // 获取群主名字
        UserInfo owner = userInfoMapper.selectById(groupInfo.getGroupOwnerId());
        groupDetailInfoVO.setGroupOwnerNickName(owner.getNickName());
        log.info("获取到群聊详细信息 {}", groupDetailInfoVO);
        return groupDetailInfoVO;
    }

    @Override
    public List<GroupDetailInfoVO> loadMyGroupInfo() {
        // 获取自己的群聊
        QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupInfo::getGroupOwnerId, UserTokenInfoContext.getCurrentUserId())
                .eq(GroupInfo::getStatus, GroupInfoStatusEnum.NORMAL);
        List<GroupInfo> list = groupInfoMapper.selectList(queryWrapper);
        List<GroupDetailInfoVO> groupDetailInfoVOList = CopyUtils.copyList(list, GroupDetailInfoVO.class);
        // 计算每个群聊的群成员数，补充群主名信息
        groupDetailInfoVOList.forEach(groupDetailInfoVO -> {
            QueryWrapper<UserContact> countWrapper = new QueryWrapper<>();
            countWrapper.lambda().eq(UserContact::getContactId, groupDetailInfoVO.getGroupId());
            Long count = userContactMapper.selectCount(countWrapper);
            groupDetailInfoVO.setMemberCount(count);
            groupDetailInfoVO.setGroupOwnerNickName(UserTokenInfoContext.getCurrentUserNickName());  // 群主名就是自己的名字
        });
        log.info("获取创建的群聊 {}", groupDetailInfoVOList);
        return groupDetailInfoVOList;
    }
}
