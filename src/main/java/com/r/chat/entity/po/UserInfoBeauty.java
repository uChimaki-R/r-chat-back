package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.r.chat.entity.enums.UserInfoBeautyStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息（靓号）
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_info_beauty")
@Builder
public class UserInfoBeauty implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户id（靓号）
     */
    private String userId;

    /**
     * 靓号使用状态 0：未使用 1：已使用
     */
    private UserInfoBeautyStatusEnum status; // 使用枚举类型接收
}
