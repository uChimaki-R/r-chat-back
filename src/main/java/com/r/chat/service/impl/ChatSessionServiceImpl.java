package com.r.chat.service.impl;

import com.r.chat.entity.po.ChatSession;
import com.r.chat.mapper.ChatSessionMapper;
import com.r.chat.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

}
