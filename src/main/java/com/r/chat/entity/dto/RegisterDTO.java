package com.r.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * UUID唯一标识
     */
    @NotEmpty
    private String checkCodeKey;

    /**
     * 验证码结果
     */
    @NotEmpty
    private String checkCode;

    /**
     * 邮箱
     */
    @NotEmpty
    private String email;

    /**
     * 密码
     */
    @NotEmpty
    private String password;

    /**
     * 用户名
     */
    @NotEmpty
    private String nickName;
}
