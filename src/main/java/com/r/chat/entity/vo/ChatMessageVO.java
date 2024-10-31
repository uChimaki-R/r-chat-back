package com.r.chat.entity.vo;

import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.entity.enums.MessageStatusEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 接收人id
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
    private MessageStatusEnum status;

    // 前端除了更新信息外，还要更新会话里的最后消息等内容，需要后端一起返回
    /**
     * 最后接收的消息
     */
    private String lastMessage;

    /**
     * 最后接收消息的时间
     */
    private Long lastReceiveTime;

}
