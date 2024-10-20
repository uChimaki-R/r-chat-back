package com.r.chat.entity.dto;

import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.utils.AvatarOwner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO implements Serializable, AvatarOwner {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String nickName;

    /**
     * 好友添加模式 0: 可以直接添加好友 1: 同意后添加好友
     */
    private JoinTypeEnum joinType;

    /**
     * 性别 0：女 1：男
     */
    @Range(min = 0, max = 1, message = Constants.VALIDATE_ILLEGAL_GENDER)
    private Integer gender;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    /**
     * 头像，一个原始，一个压缩过
     */
    private MultipartFile avatarFile;
    private MultipartFile avatarCover;

    @Override
    public String getIdentityName() {
        return UserIdContext.getCurrentUserId();
    }
}
