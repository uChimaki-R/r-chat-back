package com.r.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String email;
    private String nickName;
    private Integer joinType; // 好友添加模式 0: 可以直接添加好友 1: 同意后添加好友
    private Integer gender;
    private String password;
    private String personalSignature;
    private String areaName;
    private String areaCode;
    // 没有 status createTime lastLoginTime lastOffTime

    // 多了以下属性
    private String token;
    private boolean admin = false;
    private Integer contactStatus; // 当前状态
}
