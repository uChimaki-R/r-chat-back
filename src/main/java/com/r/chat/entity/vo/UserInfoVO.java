package com.r.chat.entity.vo;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String nickName;
    private boolean admin = false;
    private String token;
}
