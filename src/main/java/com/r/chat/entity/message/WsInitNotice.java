package com.r.chat.entity.message;

import com.r.chat.entity.enums.NoticeTypeEnum;
import com.r.chat.entity.po.ChatMessage;
import com.r.chat.entity.result.Notice;
import com.r.chat.entity.vo.ChatSessionUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 初始化ws连接后的通知，把会话信息、聊天信息、好友申请数量带到前端，让前端渲染界面
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WsInitNotice extends Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    public WsInitNotice(){
        super(NoticeTypeEnum.WS_INIT);
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
    private Long applyCount;
}
