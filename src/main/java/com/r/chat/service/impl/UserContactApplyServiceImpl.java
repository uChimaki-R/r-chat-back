package com.r.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.po.UserContactApply;
import com.r.chat.entity.vo.ContactApplyVO;
import com.r.chat.mapper.UserContactApplyMapper;
import com.r.chat.service.IUserContactApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 联系人申请 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Service
@RequiredArgsConstructor
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements IUserContactApplyService {
    private final UserContactApplyMapper userContactApplyMapper;

    @Override
    public Page<ContactApplyVO> getApplyInfoPage(Page<ContactApplyVO> page) {
        return userContactApplyMapper.selectApplyInfoPage(page, UserIdContext.getCurrentUserId());
    }
}
