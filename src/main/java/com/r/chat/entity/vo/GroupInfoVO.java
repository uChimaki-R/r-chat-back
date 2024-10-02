package com.r.chat.entity.vo;

import com.r.chat.entity.enums.JoinTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
public class GroupInfoVO implements Serializable {
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
}
