package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.result.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 发送方文件上传完毕，接收方可以接收文件的通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileUploadCompletedNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public FileUploadCompletedNotice() {
        super(NoticeTypeEnum.FILE_UPLOAD_COMPLETED);
    }

    /**
     * 文件对应的消息id
     */
    private Long messageId;
}
