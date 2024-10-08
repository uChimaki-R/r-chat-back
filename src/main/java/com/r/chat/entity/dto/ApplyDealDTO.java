package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactApplyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyDealDTO {
    /**
     * 申请id
     */
    private Integer applyId;

    /**
     * 用户设置的处理状态
     */
    private UserContactApplyStatusEnum status;
}
