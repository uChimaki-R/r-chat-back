package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatSessionUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群聊创建成功通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupCreatedNotice extends Notice {

    public GroupCreatedNotice() {
        super(NoticeTypeEnum.GROUP_CREATED);
    }

    /**
     * 用于渲染会话框的内容，lastMessage就是群聊创建成功的提醒信息
     */
    private ChatSessionUserVO chatSessionUserVO;
}
