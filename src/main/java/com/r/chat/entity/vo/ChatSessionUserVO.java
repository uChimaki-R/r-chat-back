package com.r.chat.entity.vo;

import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 联系人类型
     */
    private UserContactTypeEnum contactType;

    /**
     * 最后接收的消息
     */
    private String lastMessage;

    /**
     * 最后接收消息的时间
     */
    private Long lastReceiveTime;

    /**
     * 群聊成员数量（前端在会话界面需要群聊人数，从这里获取）用户会话的话值为0
     */
    private Long memberCount;
}
