package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactSearchResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 该联系人的类别 用户/群聊
     */
    private UserContactTypeEnum contactType;

    /**
     * 用户名/群聊名
     */
    private String nickName;

    /**
     * 和该联系人的关系状态
     */
    private UserContactStatusEnum status;

    /**
     * 联系人性别
     */
    private Integer gender;

    /**
     * 联系人地区
     */
    private String areaName;
}
