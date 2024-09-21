package com.r.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="UserInfo对象", description="用户信息")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private String userId;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "用户名")
    @TableField("nick_name")
    private String nickName;

    @ApiModelProperty(value = "好友添加模式 0: 可以直接添加好友 1: 同意后添加好友")
    @TableField("join_type")
    private Integer joinType;

    @ApiModelProperty(value = "性别 0：女 1：男")
    @TableField("gender")
    private Integer gender;

    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "个性签名")
    @TableField("personal_signature")
    private String personalSignature;

    @ApiModelProperty(value = "账号状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "最后下线时间（使用bigint记录到毫秒时间）")
    @TableField("last_off_time")
    private Long lastOffTime;

    @ApiModelProperty(value = "地区名")
    @TableField("area_name")
    private String areaName;

    @ApiModelProperty(value = "地区编号")
    @TableField("area_code")
    private String areaCode;


}
