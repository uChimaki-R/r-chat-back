package com.r.chat.mapper;

import com.r.chat.entity.po.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
