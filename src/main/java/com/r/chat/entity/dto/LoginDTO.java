package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * UUID唯一标识
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CHECK_CODE_KEY)
    private String checkCodeKey;

    /**
     * 验证码结果
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CHECK_CODE)
    private String checkCode;

    /**
     * 邮箱
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_EMAIL)
    private String email;

    /**
     * 密码
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_PASSWORD)
    private String password;

}
