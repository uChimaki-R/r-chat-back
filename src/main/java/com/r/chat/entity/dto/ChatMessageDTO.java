package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话id
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_SESSION_ID)
    private String sessionId;

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;

    /**
     * 消息内容
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_MESSAGE_CONTENT)
    @Length(max = 500, message = Constants.VALIDATE_MESSAGE_CONTENT_TOO_LONG)
    private String messageContent;

    /**
     * 接收人id
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CONTACT_ID)
    private String contactId;

    /**
     * 联系人类型
     */
    private UserContactTypeEnum contactType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private FileTypeEnum fileType;

}
