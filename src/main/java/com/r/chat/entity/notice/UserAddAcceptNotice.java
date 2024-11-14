package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 同意了对方的好友申请，二者成为了好友的通知，让前端渲染会话
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAddAcceptNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public UserAddAcceptNotice() {
        super(NoticeTypeEnum.USER_ADD_ACCEPT);
    }

    private ChatDataVO chatDataVO;
}
