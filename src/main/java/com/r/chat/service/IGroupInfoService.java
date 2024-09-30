package com.r.chat.service;

import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
