package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
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
    @Pattern(regexp = Constants.REGEX_EMAIL, message = Constants.VALIDATE_ILLEGAL_EMAIL)
    private String email;

    /**
     * 密码
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_PASSWORD)
    // 密码传过来的是加密过的
    @Pattern(regexp = Constants.REGEX_MD5, message = Constants.VALIDATE_ILLEGAL_PASSWORD)
    private String password;

}
