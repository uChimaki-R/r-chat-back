package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.AppUpdateStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUpdateReleaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_APP_UPDATE_ID)
    private Integer id;

    /**
     * 发布类型 灰度/全网
     */
    @NotNull(message = Constants.VALIDATE_EMPTY_STATUS)
    private AppUpdateStatusEnum status;

    /**
     * 灰度用户名单
     */
    private String grayscaleIds;

}
