package com.r.chat.entity.result;

import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 最后的消息
     */
    private String lastMessage;

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 拓展信息
     */
    private T extendData;

    /**
     * 发送状态 0：发送中 1：已发送
     */
    private Integer status;

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
    private Integer fileType;

    /**
     * 群聊成员数量
     */
    private Integer memberCount;
}
