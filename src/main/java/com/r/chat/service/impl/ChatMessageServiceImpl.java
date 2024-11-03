package com.r.chat.service.impl;

import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.ChatMessageDTO;
import com.r.chat.entity.dto.FileDownloadDTO;
import com.r.chat.entity.dto.FileUploadDTO;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.entity.enums.MessageStatusEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.notice.ChatNotice;
import com.r.chat.entity.notice.FileUploadCompletedNotice;
import com.r.chat.entity.po.ChatMessage;
import com.r.chat.entity.po.ChatSession;
import com.r.chat.entity.vo.ChatMessageVO;
import com.r.chat.exception.*;
import com.r.chat.mapper.ChatMessageMapper;
import com.r.chat.mapper.ChatSessionMapper;
import com.r.chat.properties.DefaultSysSettingProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 聊天信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    private final RedisUtils redisUtils;
    private final DefaultSysSettingProperties defaultSysSettingProperties;
    private final ChannelUtils channelUtils;

    @Override
    public ChatMessageVO saveMessage(ChatMessageDTO chatMessageDTO) {
        MessageTypeEnum messageType = chatMessageDTO.getMessageType();
        if (messageType == null || messageType.equals(MessageTypeEnum.NOTICE)) {
            // 为空或者想发送通知型消息（显示在消息中间的灰色消息）是不合法的
            log.warn("发送的消息类型不合法 {}", chatMessageDTO);
            throw new EnumIsNullException(Constants.MESSAGE_ENUM_ERROR);
        }
        String contactId = chatMessageDTO.getContactId();
        // 发送给的人需要在自己的好友列表中
        List<String> contactIds = redisUtils.getContactIds(UserTokenInfoContext.getCurrentUserId());
        if (contactIds == null || !contactIds.contains(contactId)) {
            // 发送给非自己的好友
            log.warn(Constants.MESSAGE_CAN_NOT_SEE_THE_FRIEND, contactId);
            // 看是群聊还是好友，提示信息不一样
            if (UserContactTypeEnum.USER.equals(chatMessageDTO.getContactType())) {
                log.warn("发送消息失败 和该用户非好友关系 userId: {}", chatMessageDTO.getContactId());
                throw new IllegalOperationException(Constants.MESSAGE_NOT_THE_FRIEND);
            } else {
                log.warn("发送消息失败 不在该群聊中 groupId: {}", chatMessageDTO.getContactId());
                throw new IllegalOperationException(Constants.MESSAGE_NOT_IN_THE_GROUP);
            }
        }
        Long now = System.currentTimeMillis();
        ChatMessage chatMessage = CopyUtils.copyBean(chatMessageDTO, ChatMessage.class);
        // 转义原本的消息，显示换行和防止注入
        chatMessage.setMessageContent(StringUtils.transStrForFront(chatMessageDTO.getMessageContent()));
        // 从redis中获取用户名称信息而不是从数据库，减少性能消耗
        UserTokenInfoDTO userTokenInfo = UserTokenInfoContext.getCurrentUserTokenInfo();
        chatMessage.setSendUserId(userTokenInfo.getUserId());
        chatMessage.setSendUserNickName(userTokenInfo.getNickName());
        chatMessage.setSendTime(now);
        // 如果是文字消息直接标记发送完毕，如果是媒体消息则标记发送中
        chatMessage.setStatus(MessageTypeEnum.TEXT.equals(messageType) ? MessageStatusEnum.SENT : MessageStatusEnum.SENDING);
        chatMessageMapper.insert(chatMessage);
        log.info("新增发送的消息 {}", chatMessage);

        // 更新会话
        String sessionId = chatMessageDTO.getSessionId();
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        String lastMessage;
        if (UserContactTypeEnum.GROUP.equals(chatMessageDTO.getContactType())) {
            // 如果是群聊，会话里显示的最后信息会带上发送者的名字
            lastMessage = userTokenInfo.getNickName() + "：" + chatMessageDTO.getMessageContent();
        } else {
            lastMessage = chatMessageDTO.getMessageContent();
        }
        // 如果是发送给机器人的话让机器人回复，最后的消息也应该是机器人回复的信息
        if (defaultSysSettingProperties.getRobotId().equals(contactId)) {
            log.info("给机器人发送了消息, 自动回复消息");
            ChatNotice chatNotice = new ChatNotice();
            // 构建机器人回复的消息（可接入ai模型）
            ChatMessage cm = new ChatMessage();
            cm.setSessionId(sessionId);
            cm.setMessageType(MessageTypeEnum.TEXT);
            cm.setMessageContent(defaultSysSettingProperties.getRobotDefaultReply());
            lastMessage = cm.getMessageContent();  // 机器人回复了的话要修改最后消息
            cm.setSendUserId(defaultSysSettingProperties.getRobotId());
            cm.setSendUserNickName(defaultSysSettingProperties.getRobotNickName());
            cm.setSendTime(now);
            cm.setContactId(userTokenInfo.getUserId());  // 回复自己
            cm.setContactType(UserContactTypeEnum.USER);
            cm.setStatus(MessageStatusEnum.SENT);
            // 同样要更新到数据库里
            chatMessageMapper.insert(cm);
            log.info("新增机器人发送的消息 {}", cm);
            ChatMessageVO chatMessageVO = CopyUtils.copyBean(cm, ChatMessageVO.class);
            chatMessageVO.setLastMessage(lastMessage);
            chatMessageVO.setLastReceiveTime(now);
            chatNotice.setChatMessageVO(chatMessageVO);
            chatNotice.setReceiveId(userTokenInfo.getUserId());  // 发回给自己
            channelUtils.sendNotice(chatNotice);
            log.info("发送收到消息的通知给发消息给机器人的用户 {}", chatNotice);
        }
        chatSession.setLastMessage(lastMessage);
        chatSession.setLastReceiveTime(now);
        chatSessionMapper.updateById(chatSession);
        log.info("更新会话消息 {}", chatSession);

        // 前端除了更新信息外，还要更新会话里的最后消息等内容，需要后端一起返回
        // 包装成了ChatMessageVO对象，比ChatMessage多了lastMessage、lastReceiveTime
        ChatMessageVO chatMessageVO = CopyUtils.copyBean(chatMessage, ChatMessageVO.class);
        chatMessageVO.setLastMessage(lastMessage);
        chatMessageVO.setLastReceiveTime(now);

        // 发送通知给接收人
        ChatNotice chatNotice = new ChatNotice();
        chatNotice.setReceiveId(contactId);
        chatNotice.setChatMessageVO(chatMessageVO);
        channelUtils.sendNotice(chatNotice);
        log.info("发送收到消息的通知给接收者 {}", chatNotice);

        return chatMessageVO;
    }

    @Override
    public void saveFile(FileUploadDTO uploadDTO) {
        ChatMessage chatMessage = chatMessageMapper.selectById(uploadDTO.getMessageId());
        if (chatMessage == null) {
            log.warn("保存文件失败: 聊天信息不存在 messageId: {}", uploadDTO.getMessageId());
            throw new ChatMessageNotExistException(Constants.MESSAGE_CHAT_MESSAGE_NOT_EXIST);
        }
        if (!UserTokenInfoContext.getCurrentUserId().equals(chatMessage.getSendUserId())) {
            // 这个消息都不是自己发的，不合法请求
            log.warn("保存文件失败: 聊天消息不属于该用户 消息的发送者为: {}", chatMessage.getSendUserId());
            throw new IllegalOperationException(Constants.MESSAGE_NOT_THE_MESSAGE_SENDER);
        }
        MultipartFile file = uploadDTO.getFile();
        MultipartFile cover = uploadDTO.getCover();
        String fileSuffix = StringUtils.getFileSuffix(Objects.requireNonNull(file.getOriginalFilename()));
        if (StringUtils.isEmpty(fileSuffix)) {
            log.warn("保存文件失败: 文件格式错误");
            throw new FileNameErrorException(Constants.MESSAGE_FILE_NAME_ERROR);
        }
        FileTypeEnum fileType;
        if (Arrays.asList(Constants.IMAGE_SUFFIXES).contains(fileSuffix)) {
            // 图片类型
            if (file.getSize() > defaultSysSettingProperties.getMaxImageSize() * Constants.MB_TO_BYTE) {
                // 超出大小限制
                log.warn("保存图片文件失败: 图片文件大小 {} > {}", file.getSize(), defaultSysSettingProperties.getMaxImageSize() * Constants.MB_TO_BYTE);
                throw new FileSizeLimitException(Constants.MESSAGE_FILE_SIZE_LIMIT);
            }
            fileType = FileTypeEnum.IMAGE;
        } else if (Arrays.asList(Constants.VIDEO_SUFFIXES).contains(fileSuffix)) {
            // 视频类型
            if (file.getSize() > defaultSysSettingProperties.getMaxVideoSize() * Constants.MB_TO_BYTE) {
                // 超出大小限制
                log.warn("保存视频文件失败: 视频文件大小 {} > {}", file.getSize(), defaultSysSettingProperties.getMaxVideoSize() * Constants.MB_TO_BYTE);
                throw new FileSizeLimitException(Constants.MESSAGE_FILE_SIZE_LIMIT);
            }
            fileType = FileTypeEnum.VIDEO;
        } else {
            // 其他文件类型
            if (file.getSize() > defaultSysSettingProperties.getMaxFileSize() * Constants.MB_TO_BYTE) {
                // 超出大小限制
                log.warn("保存文件失败: 文件大小 {} > {}", file.getSize(), defaultSysSettingProperties.getMaxFileSize() * Constants.MB_TO_BYTE);
                throw new FileSizeLimitException(Constants.MESSAGE_FILE_SIZE_LIMIT);
            }
            fileType = FileTypeEnum.OTHER;
        }
        // 保存文件
        FileUtils.saveChatFile(file, cover, chatMessage.getSendTime(), fileType, chatMessage.getMessageId());

        // 更新信息为已发送
        chatMessage.setStatus(MessageStatusEnum.SENT);
        chatMessageMapper.updateById(chatMessage);
        log.info("更新消息状态为已发送 {}", chatMessage);

        // 发送通知通知接收方可以获取文件数据了
        FileUploadCompletedNotice notice = new FileUploadCompletedNotice();
        notice.setMessageId(chatMessage.getMessageId());
        notice.setReceiveId(chatMessage.getContactId());
        channelUtils.sendNotice(notice);
        log.info("发送发送方文件上传完毕，接收方可以接收文件的ws通知 {}", notice);
    }

    @Override
    public File getFile(FileDownloadDTO fileInfo) {
        // 判断文件名是否纯数字，纯数字则是聊天文件（messageId），非纯数字则是头像文件
        if (StringUtils.isNumber(fileInfo.getFileName())) {
            // 聊天文件
            String messageId = fileInfo.getFileName();
            // 获取聊天信息
            ChatMessage chatMessage = chatMessageMapper.selectById(Long.parseLong(messageId));
            if (chatMessage == null) {
                log.warn("获取聊天文件失败: 聊天信息不存在 messageId: {}", messageId);
                throw new ChatMessageNotExistException(Constants.MESSAGE_CHAT_MESSAGE_NOT_EXIST);
            }
            // 聊天的对象需要是自己的好友才行
            List<String> contactIds = redisUtils.getContactIds(UserTokenInfoContext.getCurrentUserId());
            if (contactIds == null || !contactIds.contains(chatMessage.getContactId())) {
                log.warn("获取聊天文件失败: 与 {} 非好友状态", chatMessage.getContactId());
                if (UserContactTypeEnum.USER.equals(chatMessage.getContactType())) {
                    throw new IllegalOperationException(Constants.MESSAGE_NOT_THE_FRIEND);
                } else {
                    throw new IllegalOperationException(Constants.MESSAGE_NOT_IN_THE_GROUP);
                }
            }
            return FileUtils.getChatFile(chatMessage.getSendTime(), chatMessage.getFileType(), chatMessage.getFileName(), chatMessage.getMessageId(), fileInfo.getIsCover());
        } else {
            // 头像文件
            return FileUtils.getAvatarFile(fileInfo.getFileName(), fileInfo.getIsCover());
        }
    }
}
