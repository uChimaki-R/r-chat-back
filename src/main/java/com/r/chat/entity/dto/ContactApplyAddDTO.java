package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactApplyAddDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 接收人id（加群的时候是群主id）
     */
    private String receiveUserId;

    /**
     * 联系人类型：0：好友 1：群聊
     */
    private UserContactTypeEnum contactType;

    /**
     * 联系人id（加群的时候是群聊id）
     */
    private String contactId;
}
