package com.r.chat.service.impl;

import com.r.chat.entity.po.ChatMessage;
import com.r.chat.mapper.ChatMessageMapper;
import com.r.chat.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

}
