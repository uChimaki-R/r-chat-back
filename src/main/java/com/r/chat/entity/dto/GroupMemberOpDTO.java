package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.GroupMemberOpTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberOpDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID)
    private String groupId;

    @NotEmpty(message = Constants.VALIDATE_EMPTY_CONTACT_ID)
    private String contactIds;  // 批量操作

    @NotNull(message = Constants.VALIDATE_EMPTY_OPERATE_TYPE)
    private GroupMemberOpTypeEnum opType;
}
