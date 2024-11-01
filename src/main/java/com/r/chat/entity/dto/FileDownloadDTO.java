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
public class FileDownloadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 需要下载的文件名。如果是头像的话则是U/G+数字，如果是聊天文件的话则是messageId
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_FILE_NAME)
    private String fileName;

    /**
     * 要下载的是否是缩略图
     */
    private Boolean isCover;
}
