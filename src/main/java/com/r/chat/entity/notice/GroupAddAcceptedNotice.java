package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatMessageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 群聊加入申请被通过的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupAddAcceptedNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public GroupAddAcceptedNotice() {
        super(NoticeTypeEnum.GROUP_ADD_ACCEPTED);
    }

    /**
     * 用于渲染会话框的内容，lastMessage就是自己加入群聊的提示信息
     */
    private ChatMessageVO chatMessageVO;
}
