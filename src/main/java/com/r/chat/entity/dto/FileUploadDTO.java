package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = Constants.VALIDATE_EMPTY_MESSAGE_ID)
    private Long messageId;

    @NotNull(message = Constants.VALIDATE_EMPTY_MULTIPART_FILE)
    private MultipartFile file;

    /**
     * 缩略图，图片文件和视频文件会有
     */
    private MultipartFile cover;
}
