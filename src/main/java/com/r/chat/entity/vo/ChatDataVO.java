package com.r.chat.entity.vo;

import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.entity.enums.MessageStatusEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.ChatMessage;
import com.r.chat.entity.po.ChatSession;
import com.r.chat.utils.CopyUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 综合了三张表的数据的VO类
 * 前端使用同一个类更新两个表的数据（通过判断是否表段来选择属性）
 * （前端把chat_session和chat_session_user合为了一张表chat_session_user）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 构造对象
     */
    public static ChatDataVO fromChatData(ChatMessage chatMessage, ChatSession chatSession, String contactId, String contactName) {
        ChatDataVO chatDataVO = CopyUtils.copyBean(chatMessage, ChatDataVO.class);
        chatDataVO.setLastMessage(chatSession.getLastMessage());
        chatDataVO.setLastReceiveTime(chatSession.getLastReceiveTime());
        chatDataVO.setContactId(contactId);
        chatDataVO.setContactName(contactName);
        return chatDataVO;
    }

    /* chat_message */

    /**
     * 自增id
     */
    private Long messageId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 发送人id
     */
    private String sendUserId;

    /**
     * 发送人昵称
     */
    private String sendUserNickName;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 联系人类型
     */
    private UserContactTypeEnum contactType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private FileTypeEnum fileType;

    /**
     * 状态 0：正在发送 1：已发送
     */
    private MessageStatusEnum sendStatus;

    /* chat_session */

    /**
     * 最后接收的消息
     */
    private String lastMessage;

    /**
     * 最后接收消息的时间
     */
    private Long lastReceiveTime;

    /* chat_session_user */

    /**
     * 联系人名称
     */
    private String contactName;

    /* 群聊还需要有成员数 */

    /**
     * 群聊人数
     */
    private Long memberCount;

}
