package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    private Integer id;

    /**
     * 版本号
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_VERSION)
    @Pattern(regexp = Constants.REGEX_VERSION, message = Constants.VALIDATE_ILLEGAL_VERSION)
    private String version;

    /**
     * 更新描述
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_DESCRIPTION)
    private String description;

    /**
     * 更新手段 0：文件 1：外链
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_METHOD_TYPE)
    private AppUpdateMethodTypeEnum methodType;

    /**
     * 外链地址
     */
    private String outerLink;

    /**
     * exe文件
     */
    private MultipartFile file;

}
