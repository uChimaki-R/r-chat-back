package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * UUID唯一标识
     */
    private String checkCodeKey;

    /**
     * 验证码结果
     */
    private String checkCode;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

}
