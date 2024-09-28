package com.r.chat.service.impl;

import com.r.chat.entity.po.GroupInfo;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群组信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IGroupInfoService {

}
