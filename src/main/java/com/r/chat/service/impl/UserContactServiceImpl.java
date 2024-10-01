package com.r.chat.service.impl;

import com.r.chat.entity.dto.GroupMemberInfoDTO;
import com.r.chat.entity.po.UserContact;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户联系人 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Service
@RequiredArgsConstructor
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements IUserContactService {
    private final UserContactMapper userContactMapper;

    @Override
    public List<GroupMemberInfoDTO> getGroupMemberInfo(String groupId) {
        return userContactMapper.selectGroupMemberByGroupId(groupId);
    }
}
