package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.r.chat.entity.enums.MessageStatusEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 聊天信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 会话id
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 消息类型
     */
    @TableField("message_type")
    private MessageTypeEnum messageType;

    /**
     * 消息内容
     */
    @TableField("message_content")
    private String messageContent;

    /**
     * 发送人id
     */
    @TableField("send_user_id")
    private String sendUserId;

    /**
     * 发送人昵称
     */
    @TableField("send_user_nick_name")
    private String sendUserNickName;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Long sendTime;

    /**
     * 接收人id
     */
    @TableField("contact_id")
    private String contactId;

    /**
     * 联系人类型
     */
    @TableField("contact_type")
    private UserContactTypeEnum contactType;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private Integer fileType;

    /**
     * 状态 0：正在发送 1：已发送
     */
    @TableField("status")
    private MessageStatusEnum status;

}
