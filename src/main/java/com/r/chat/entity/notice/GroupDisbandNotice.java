package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 群聊已解散的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupDisbandNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public GroupDisbandNotice() {
        super(NoticeTypeEnum.GROUP_DISBAND);
    }

    private ChatDataVO chatDataVO;
}
