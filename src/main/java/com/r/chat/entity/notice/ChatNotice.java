package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 收到聊天消息的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public ChatNotice() {
        super(NoticeTypeEnum.CHAT);
    }

    private ChatDataVO chatDataVO;
}
