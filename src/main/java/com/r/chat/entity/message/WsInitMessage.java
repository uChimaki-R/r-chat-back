package com.r.chat.entity.message;

import com.r.chat.entity.enums.MessageTypeEnum;
import com.r.chat.entity.po.ChatMessage;
import com.r.chat.entity.result.Message;
import com.r.chat.entity.vo.ChatSessionUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WsInitMessage extends Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public WsInitMessage(){
        super(MessageTypeEnum.WS_INIT);
    }

    /**
     * 会话信息列表
     */
    private List<ChatSessionUserVO> chatSessionUserList;

    /**
     * 聊天消息列表
     */
    private List<ChatMessage> chatMessageList;

    /**
     * 好友申请数量
     */
    private Integer applyCount;
}
