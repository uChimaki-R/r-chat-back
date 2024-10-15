package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String userId;

    @NotNull(message = Constants.VALIDATE_EMPTY_STATUS)
    private UserStatusEnum status;
}
