package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.entity.enums.UserInfoStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private String userId;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 用户名
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 好友添加模式 0: 可以直接添加好友 1: 同意后添加好友
     */
    @TableField("join_type")
    private JoinTypeEnum joinType;

    /**
     * 性别 0：女 1：男
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 个性签名
     */
    @TableField(value = "personal_signature", updateStrategy = FieldStrategy.NOT_NULL)  // 个性签名可以为空字符串，为null不更新（已经全局设置null/空字符串不更新）
    private String personalSignature;

    /**
     * 账号状态
     */
    @TableField("status")
    private UserInfoStatusEnum status; // 使用枚举类型接收

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后下线时间（使用bigint记录到毫秒时间）
     */
    @TableField("last_off_time")
    private Long lastOffTime;

    /**
     * 地区名
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 地区编号
     */
    @TableField("area_code")
    private String areaCode;


}
