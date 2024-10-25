package com.r.chat.entity.result;

import com.r.chat.entity.enums.NoticeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public Notice(NoticeTypeEnum type) {
        this.messageType = type;
    }

    /**
     * 通知的接收人id
     */
    @Setter
    private String receiveId;

    /**
     * 通知类型
     */
    private NoticeTypeEnum messageType;
}
