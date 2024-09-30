package com.r.chat.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 群组信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群组id
     */
    private String groupId;

    /**
     * 群组名
     */
    private String groupName;

    /**
     * 群主id
     */
    private String groupOwnerId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 加群类型：0：直接加入 1：管理员同意后加入
     */
    private Integer joinType;

    /**
     * 状态：1：正常 0：解散
     */
    private GroupInfoStatusEnum status;
}
