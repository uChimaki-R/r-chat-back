package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;

    private String nickName;
}
