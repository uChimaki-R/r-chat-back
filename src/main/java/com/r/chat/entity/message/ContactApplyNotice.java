package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.po.UserContactApply;
import com.r.chat.entity.result.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 有新的好友申请的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContactApplyNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public ContactApplyNotice(){
        super(NoticeTypeEnum.CONTACT_APPLY);
    }

    /**
     * 好友申请内容
     */
    private UserContactApply userContactApply;
}
