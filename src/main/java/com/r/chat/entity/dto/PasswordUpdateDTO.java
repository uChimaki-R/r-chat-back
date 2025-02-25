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
public class PasswordUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String oldPassword;

    @NotEmpty(message = Constants.VALIDATE_EMPTY_PASSWORD)
    @Pattern(regexp = Constants.REGEX_PASSWORD, message = Constants.VALIDATE_ILLEGAL_PASSWORD)
    private String newPassword;
}
