package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatMessageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收到聊天消息的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatNotice extends Notice {
    public ChatNotice() {
        super(NoticeTypeEnum.CHAT);
    }

    /**
     * 发送的消息内容和lastReceiveTime、lastMessage
     */
    private ChatMessageVO chatMessageVO;
}
