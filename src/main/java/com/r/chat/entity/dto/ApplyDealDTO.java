package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.UserContactApplyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyDealDTO {
    /**
     * 申请id
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_APPLY_ID)
    private Integer applyId;

    /**
     * 用户设置的处理状态
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_CONTACT_APPLY_STATUS)
    private UserContactApplyStatusEnum status;
}
