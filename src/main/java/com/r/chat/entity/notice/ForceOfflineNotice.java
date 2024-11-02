package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;

import java.io.Serializable;

/**
 * 被管理员强制下线的通知
 */
public class ForceOfflineNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public ForceOfflineNotice() {
        super(NoticeTypeEnum.FORCE_OFFLINE);
    }
}
