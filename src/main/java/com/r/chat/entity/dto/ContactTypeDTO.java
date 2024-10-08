package com.r.chat.entity.dto;

import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 联系人类型
     */
    private UserContactTypeEnum contactType;
}
