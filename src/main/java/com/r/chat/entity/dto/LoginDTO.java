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
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String checkCodeKey;
    private String checkCode;
    private String email;
    private String password;
}
