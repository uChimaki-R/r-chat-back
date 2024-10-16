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
public class BeautyUserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    private Integer id;

    /**
     * 邮箱
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_EMAIL)
    @Pattern(regexp = Constants.REGEX_EMAIL, message = Constants.VALIDATE_ILLEGAL_EMAIL)
    private String email;

    /**
     * 用户id（靓号）
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_USER_ID)
    private String userId;
}
