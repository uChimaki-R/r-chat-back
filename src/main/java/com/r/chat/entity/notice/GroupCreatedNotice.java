package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 群聊创建成功通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupCreatedNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public GroupCreatedNotice() {
        super(NoticeTypeEnum.GROUP_CREATED);
    }

    private ChatDataVO chatDataVO;
}
