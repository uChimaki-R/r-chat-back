package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 联系人类型
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_CONTACT_TYPE)
    private UserContactTypeEnum contactType;
}
