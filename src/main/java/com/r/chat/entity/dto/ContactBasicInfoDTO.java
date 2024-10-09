package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactBasicInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String nickName;

    /**
     * 性别 0：女 1：男
     */
    private Integer gender;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    /**
     * 获取该信息（点击了名片）的用户和该用户的关系
     */
    private UserContactStatusEnum contactStatus;
}
