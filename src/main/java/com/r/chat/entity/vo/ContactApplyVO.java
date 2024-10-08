package com.r.chat.entity.vo;

import com.r.chat.entity.enums.UserContactApplyStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactApplyVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 申请信息的自增id，返回给前端，处理申请时由前端带回
     */
    private Integer applyId;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 申请人名称
     */
    private String applyUserName;

    /**
     * 联系人类型：0：好友 1：群聊
     */
    private UserContactTypeEnum contactType;

    /**
     * 群聊id
     */
    private String groupId;

    /**
     * 群聊名称（如果是群聊加入的申请）
     */
    private String groupName;

    /**
     * 状态：0：待处理 1：已同意 2：已拒绝 3：已拉黑
     */
    private UserContactApplyStatusEnum status;

    /**
     * 申请信息
     */
    private String applyInfo;
}
