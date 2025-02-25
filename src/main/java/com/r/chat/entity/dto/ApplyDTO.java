package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 打算添加的联系人id
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CONTACT_ID)
    private String contactId;

    /**
     * 申请信息
     */
    private String applyInfo;
}
