package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.context.AdminContext;
import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.GroupInfoQueryDTO;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.enums.*;
import com.r.chat.entity.message.GroupCreatedNotice;
import com.r.chat.entity.message.ContactRenameNotice;
import com.r.chat.entity.message.GroupDisbandNotice;
import com.r.chat.entity.po.*;
import com.r.chat.entity.vo.ChatMessageVO;
import com.r.chat.entity.vo.ChatSessionUserVO;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.exception.GroupCountLimitException;
import com.r.chat.exception.GroupNotExistException;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.mapper.ChatMessageMapper;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IChatSessionService;
import com.r.chat.service.IChatSessionUserService;
import com.r.chat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IGroupInfoService {
    private final IChatSessionService chatSessionServiceImpl;
    private final IChatSessionUserService chatSessionUserServiceImpl;

    private final UserContactMapper userContactMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final ChatMessageMapper chatMessageMapper;

    private final RedisUtils redisUtils;
    private final ChannelUtils channelUtils;

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

            // todo 暂时注释掉这段代码，让前端先跳过上传图片的逻辑，后续需要再放开
//            // 没有携带群头像
//            if (groupInfoDTO.getAvatarFile() == null) {
//                log.warn("拒绝新增群聊: 未指定群头像");
//                throw new ParameterErrorException(Constants.MESSAGE_MISSING_FILE);
//            }

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
            chatMessage.setStatus(MessageStatusEnum.SENT);
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
            ChatSessionUserVO chatSessionUserVO = CopyUtils.copyBean(chatSessionUser, ChatSessionUserVO.class);
            chatSessionUserVO.setLastMessage(Constants.MESSAGE_GROUP_CREATED);
            chatSessionUserVO.setLastReceiveTime(millis);
            chatSessionUserVO.setContactType(UserContactTypeEnum.GROUP);
            chatSessionUserVO.setMemberCount(1L);  // 初始只有自己一个人
            groupCreatedNotice.setChatSessionUserVO(chatSessionUserVO);
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
        // 头像文件的操作
        if (groupInfoDTO.getAvatarFile() == null) {
            return;
        }
        // 头像保存到本地
        FileUtils.saveAvatarFile(groupInfoDTO);
    }

    @Override
    public Page<GroupDetailInfoVO> loadGroupDetailInfo(Page<GroupDetailInfoVO> page, GroupInfoQueryDTO groupInfoQueryDTO) {
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
        if (!groupInfo.getGroupOwnerId().equals(UserTokenInfoContext.getCurrentUserId()) || !AdminContext.isAdmin()) {
            log.warn("解散群聊失败: 非群主或管理员操作 操作人id: {}, groupId: {}, 群主id: {}", UserTokenInfoContext.getCurrentUserId(), groupId, groupInfo.getGroupOwnerId());
            throw new IllegalOperationException(Constants.MESSAGE_ILLEGAL_OPERATION);
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

        // 更新会话
        String sessionId = StringUtils.getSessionId(new String[]{groupId});
        Long now = System.currentTimeMillis();
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(Constants.MESSAGE_GROUP_DISBAND);
        chatSession.setLastReceiveTime(now);
        chatSessionServiceImpl.saveOrUpdate(chatSession);
        log.info("更新群聊会话 {}", chatSession);

        // 新增这条解散信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.NOTICE);
        chatMessage.setMessageContent(Constants.MESSAGE_GROUP_DISBAND);
        chatMessage.setSendTime(now);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP);
        chatMessage.setStatus(MessageStatusEnum.SENT);
        chatMessageMapper.insert(chatMessage);
        log.info("新增解散群聊的消息 {}", chatMessage);

        // 发送群聊解散通知
        GroupDisbandNotice notice = new GroupDisbandNotice();
        ChatMessageVO chatMessageVO = CopyUtils.copyBean(chatMessage, ChatMessageVO.class);
        chatMessageVO.setLastReceiveTime(now);
        chatMessageVO.setLastMessage(Constants.MESSAGE_GROUP_DISBAND);
        notice.setChatMessageVO(chatMessageVO);
        notice.setReceiveId(groupId);
        channelUtils.sendNotice(notice);
        log.info("发送群聊解散的ws通知 {}", notice);

        log.info("解散群聊成功 groupId: {}", groupId);
    }
}
