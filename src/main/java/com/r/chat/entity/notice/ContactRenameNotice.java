package com.r.chat.entity.notice;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 联系人名称修改通知，可以是群聊名称修改了，也可以是用户名称修改了
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContactRenameNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public ContactRenameNotice() {
        super(NoticeTypeEnum.CONTACT_RENAME);
    }

    /**
     * 需要更新名称的联系人id
     */
    private String contactId;

    /**
     * 更新的名称
     */
    private String contactName;
}
