package com.r.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckCodeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String checkCode;
    private String checkCodeKey;
}
