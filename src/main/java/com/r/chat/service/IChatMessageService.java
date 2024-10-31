package com.r.chat.service;

import com.r.chat.entity.dto.ChatMessageDTO;
import com.r.chat.entity.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.r.chat.entity.vo.ChatMessageVO;

/**
 * <p>
 * 聊天信息 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 保存用户发送的信息，更新会话信息并将更新的会话信息返回给前端用于前端的数据库更新
     */
    ChatMessageVO saveMessage(ChatMessageDTO chatMessageDTO);
}
