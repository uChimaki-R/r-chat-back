package com.r.chat.service.impl;

import com.github.jeffreyning.mybatisplus.service.MppServiceImpl;
import com.r.chat.entity.po.ChatSessionUser;
import com.r.chat.mapper.ChatSessionUserMapper;
import com.r.chat.service.IChatSessionUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话用户（多对多） 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Service
public class ChatSessionUserServiceImpl extends MppServiceImpl<ChatSessionUserMapper, ChatSessionUser> implements IChatSessionUserService {

}
