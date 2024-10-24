package com.r.chat.entity.result;

import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public Message(MessageTypeEnum type) {
        this.messageType = type;
    }

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 发送人id
     */
    private String sendUserId;

    /**
     * 发送人昵称
     */
    private String sendUserNickName;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 联系人类型
     */
    private UserContactTypeEnum contactType;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息类型
     */
    private final MessageTypeEnum messageType;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 发送状态 0：发送中 1：已发送
     */
    private Integer status;
}
