package com.r.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户/群聊基础信息，包括id和名称
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String userOrGroupId;  // 本来名字想写成id的，但是感觉sql查询可能有报错隐患

    /**
     * 昵称
     */
    private String userOrGroupName;
}
