package com.r.chat.entity.result;

import com.r.chat.entity.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public Message(MessageTypeEnum type) {
        this.messageType = type;
    }

    /**
     * 发送人id
     */
    @Setter
    private String sendId;

    /**
     * 联系人id
     */
    @Setter
    private String receiveId;

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;
}
