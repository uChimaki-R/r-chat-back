package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO implements Serializable {
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
    @Pattern(regexp = Constants.REGEX_EMAIL, message = Constants.VALIDATE_ILLEGAL_EMAIL)
    private String email;

    /**
     * 密码
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_PASSWORD)
    @Pattern(regexp = Constants.REGEX_PASSWORD, message = Constants.VALIDATE_ILLEGAL_PASSWORD)
    private String password;

    /**
     * 用户名
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_NICKNAME)
    @Pattern(regexp = Constants.REGEX_NICK_NAME, message = Constants.VALIDATE_ILLEGAL_NICK_NAME)
    private String nickName;
}
