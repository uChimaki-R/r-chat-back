package com.r.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.GroupInfoQueryDTO;
import com.r.chat.entity.dto.GroupMemberOpDTO;
import com.r.chat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.r.chat.entity.vo.GroupDetailInfoVO;

import java.util.List;

/**
 * <p>
 * 群聊信息 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
public interface IGroupInfoService extends IService<GroupInfo> {

    /**
     * 新增或修改群聊信息
     */
    void saveOrUpdateGroupInfo(GroupInfoDTO groupInfoDTO);

    /**
     * 管理员分页多表联查查询群聊信息，包括群主的名称和群成员数量
     */
    Page<GroupDetailInfoVO> loadGroupInfo4Admin(Page<GroupDetailInfoVO> page, GroupInfoQueryDTO groupInfoQueryDTO);

    /**
     * 解散群聊
     */
    void disbandGroup(String groupId);

    /**
     * 新增或移除群成员
     */
    void addOrRemoveGroupMember(GroupMemberOpDTO opInfo);

    /**
     * 将用户idToLeave移出群聊（可能是自己退出的，也可能是群主移出的）
     */
    void leaveGroup(String idToLeave, String groupId);

    /**
     * 获取群聊信息
     */
    GroupDetailInfoVO getGroupDetailInfo(String groupId);

    /**
     * 获取自己创建的群聊的信息
     */
    List<GroupDetailInfoVO> loadMyGroupInfo();
}
