package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatMessageVO;
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

    /**
     * 发送的消息内容和lastReceiveTime、lastMessage、新的群聊人数等
     */
    private ChatMessageVO chatMessageVO;

    /**
     * 离开群聊的用户id
     */
    private String leaveUserId;
}
