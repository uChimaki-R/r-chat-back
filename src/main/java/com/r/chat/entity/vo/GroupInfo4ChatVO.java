package com.r.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfo4ChatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群聊信息
     */
    private GroupDetailInfoVO groupInfo;

    /**
     * 群成员信息
     */
    private List<BasicInfoVO> userContactList;
}
