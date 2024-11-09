package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 离开群聊或者被移出群聊的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupMemberLeaveOrIsRemovedNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public GroupMemberLeaveOrIsRemovedNotice() {
        super(NoticeTypeEnum.GROUP_MEMBER_LEAVE_OR_IS_REMOVED);
    }

    private ChatDataVO chatDataVO;

    /**
     * 离开群聊的用户id
     */
    private String leaveUserId;
}
