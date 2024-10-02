package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.UserContactApplyStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 联系人申请
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_contact_apply")
public class UserContactApply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

    /**
     * 申请人id
     */
    @TableField("apply_user_id")
    private String applyUserId;

    /**
     * 接收人id（加群的时候是群主id）
     */
    @TableField("receive_user_id")
    private String receiveUserId;

    /**
     * 联系人类型：0：好友 1：群聊
     */
    @TableField("contact_type")
    private UserContactTypeEnum contactType;

    /**
     * 联系人id（加群的时候是群聊id）
     */
    @TableField("contact_id")
    private String contactId;

    /**
     * 最后申请时间
     */
    @TableField("last_apply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastApplyTime;

    /**
     * 状态：0：待处理 1：已同意 2：已拒绝 3：已拉黑
     */
    @TableField("status")
    private UserContactApplyStatusEnum status;

    /**
     * 申请信息
     */
    @TableField("apply_info")
    private String applyInfo;


}
