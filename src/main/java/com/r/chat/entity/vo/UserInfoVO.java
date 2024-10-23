package com.r.chat.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.entity.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO implements Serializable {

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
     * 用户名
     */
    private String nickName;

    /**
     * 好友添加模式 0: 可以直接添加好友 1: 同意后添加好友
     */
    private JoinTypeEnum joinType;

    /**
     * 性别 0：女 1：男
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
     * 账号状态
     */
    private UserStatusEnum status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后下线时间（使用bigint记录到毫秒时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOffTime;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;


}
