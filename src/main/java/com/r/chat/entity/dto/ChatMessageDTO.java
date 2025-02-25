package com.r.chat.entity.dto;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
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
     * 联系人id
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CONTACT_ID)
    private String contactId;

    /**
     * 联系人名称，只在发送到群聊时有用
     * 当contactType为群聊时使用这个名称（群聊的所有人看到的会话上的名字是群聊名）
     * 否则则是发送给用户的，对方看到的是自己的名字，所以从上下文取自己的名字即可
     * （让前端把群聊名发过来后端就不用根据contactId再查群聊名，降低压力，用户名字同理）
     */
    @NotEmpty(message = Constants.VALIDATE_EMPTY_CONTACT_NAME)
    private String contactName;

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
