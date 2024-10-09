package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactTypeEnum;
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
public class BasicInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 群聊id
     */
    private String groupId;

    /**
     * 群聊名
     */
    private String groupName;

    /**
     * 类型 用户/群聊
     */
    private UserContactTypeEnum contactType;
}
