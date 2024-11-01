package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatSessionUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 对方同意了自己的申请，二者成为了好友的通知，让前端渲染会话
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAddByOthersNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public UserAddByOthersNotice() {
        super(NoticeTypeEnum.USER_ADD_BY_OTHERS);
    }

    /**
     * 用于渲染会话框的内容，lastMessage就是申请信息
     */
    private ChatSessionUserVO chatSessionUserVO;
}
