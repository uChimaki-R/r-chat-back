package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 好友添加模式 0: 可以直接添加好友 1: 同意后添加好友
     */
    private Integer joinType;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String password;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    /**
     * token
     */
    private String token;

    /**
     * 是否是管理员账号
     */
    private boolean admin = false;

    /**
     * 当前状态
     */
    private Integer contactStatus;
}
