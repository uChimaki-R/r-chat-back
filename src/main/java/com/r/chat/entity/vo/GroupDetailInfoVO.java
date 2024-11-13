package com.r.chat.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import com.r.chat.entity.enums.JoinTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 群聊信息
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDetailInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群聊id
     */
    private String groupId;

    /**
     * 群聊名
     */
    private String groupName;

    /**
     * 群主id
     */
    private String groupOwnerId;

    /**
     * 群主名
     */
    private String groupOwnerNickName;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 加群类型：0：直接加入 1：管理员同意后加入
     */
    private JoinTypeEnum joinType;

    /**
     * 群成员数
     */
    private Long memberCount;

    /**
     * 状态
     */
    private GroupInfoStatusEnum status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
