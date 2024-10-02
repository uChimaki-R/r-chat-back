package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 打算添加的联系人id
     */
    private String contactId;

    /**
     * 申请信息
     */
    private String applyInfo;
}
