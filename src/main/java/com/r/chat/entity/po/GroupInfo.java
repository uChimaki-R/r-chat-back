package com.r.chat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import com.r.chat.entity.enums.JoinTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 群聊信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("group_info")
public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群聊id
     */
    @TableId(value = "group_id", type = IdType.AUTO)
    private String groupId;

    /**
     * 群聊名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 群主id
     */
    @TableField("group_owner_id")
    private String groupOwnerId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 群公告
     */
    @TableField("group_notice")
    private String groupNotice;

    /**
     * 加群类型：0：直接加入 1：管理员同意后加入
     */
    @TableField("join_type")
    private JoinTypeEnum joinType;

    /**
     * 状态：0：正常 1：解散
     */
    @TableField("status")
    private GroupInfoStatusEnum status;
}
