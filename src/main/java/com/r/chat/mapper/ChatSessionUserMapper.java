package com.r.chat.mapper;

import com.github.jeffreyning.mybatisplus.base.MppBaseMapper;
import com.r.chat.entity.po.ChatSessionUser;
import com.r.chat.entity.vo.ChatSessionUserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 会话用户（多对多） Mapper 接口
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
@Mapper
public interface ChatSessionUserMapper extends MppBaseMapper<ChatSessionUser> {

    /**
     * 根据用户id获取该用户的全部会话信息，需要和chat_session表联查获取具体信息
     * 如果是群聊，还要和user_contact表联查获取该群聊的群成员数量
     */
    List<ChatSessionUserVO> selectChatSessionUserVOList(String userId);
}
