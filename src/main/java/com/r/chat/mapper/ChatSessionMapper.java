package com.r.chat.mapper;

import com.r.chat.entity.po.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 会话信息 Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}
