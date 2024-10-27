package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatSessionUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群聊加入申请被通过的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupAddAcceptedNotice extends Notice {
    public GroupAddAcceptedNotice() {
        super(NoticeTypeEnum.GROUP_ADD_ACCEPTED);
    }

    /**
     * 用于渲染会话框的内容，lastMessage就是自己加入群聊的提示信息
     */
    private ChatSessionUserVO chatSessionUserVO;
}
