package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String checkCodeKey; // UUID唯一标识
    private String checkCode; // 发送的验证码结果
    private String email;
    private String password;
    private String nickName;
}
