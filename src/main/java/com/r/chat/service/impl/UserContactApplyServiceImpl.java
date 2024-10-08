package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.ApplyDealDTO;
import com.r.chat.entity.dto.ContactApplyAddDTO;
import com.r.chat.entity.enums.UserContactApplyStatusEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.po.UserContact;
import com.r.chat.entity.po.UserContactApply;
import com.r.chat.entity.vo.ContactApplyVO;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.exception.ParameterErrorException;
import com.r.chat.mapper.UserContactApplyMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.service.IUserContactApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.VerifyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 联系人申请 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements IUserContactApplyService {
    private final IUserContactService userContactService;

    private final UserContactApplyMapper userContactApplyMapper;
    private final UserContactMapper userContactMapper;

    @Override
    public Page<ContactApplyVO> getApplyInfoPage(Page<ContactApplyVO> page) {
        return userContactApplyMapper.selectApplyInfoPage(page, UserIdContext.getCurrentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(ApplyDealDTO applyDealDTO) {
        LocalDateTime now = LocalDateTime.now();
        Integer applyId = applyDealDTO.getApplyId();
        UserContactApplyStatusEnum status = applyDealDTO.getStatus();
        if (status == null || UserContactApplyStatusEnum.PENDING.equals(status)) {
            log.warn("传入的申请状态不合法 status: {}", status);
            throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        // 查询这个申请
        UserContactApply userContactApply = userContactApplyMapper.selectById(applyId);
        if (userContactApply == null) {
            log.warn("不存在该申请 applyId: {}", applyId);
            throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        // 需要是本人操作
        VerifyUtils.assertIsCurrentUser(userContactApply.getReceiveUserId());
        if (!UserContactApplyStatusEnum.PENDING.equals(userContactApply.getStatus())) {
            // 该申请不处于待处理的状态，代表该申请已经被处理过了更新过了状态，该请求为重复操作，为了健壮性，应该拒绝该操作
            log.warn("申请不处于待处理的状态, 拒绝操作 当前状态: {}", userContactApply.getStatus());
            throw new IllegalOperationException(Constants.MESSAGE_REPETITIVE_OPERATION);
        }
        // 修改申请状态
        userContactApply.setStatus(status);
        userContactApply.setLastApplyTime(now);
        updateById(userContactApply);
        switch (status) {
            case AGREED:
                // 添加联系人
                ContactApplyAddDTO contactApplyAddDTO = CopyUtils.copyBean(userContactApply, ContactApplyAddDTO.class);
                userContactService.addContact(contactApplyAddDTO);
                break;
            case REJECTED:
                // 拒绝无需处理
                log.info("拒绝该申请");
                break;
            case BLOCKED:
                // 拉黑，需要把联系人的状态也设置为拉黑
                // 先查一下有没有关系
                log.info("拉黑申请人 {}", userContactApply.getApplyUserId());
                QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda()
                        .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())  // 自己
                        .eq(UserContact::getContactId, userContactApply.getApplyUserId());  // 和申请人
                UserContact userContact = userContactMapper.selectOne(queryWrapper);
                if (userContact == null) {
                    // 新增拉黑关系
                    UserContact blockContact = new UserContact();
                    blockContact.setUserId(UserIdContext.getCurrentUserId());
                    blockContact.setContactId(userContactApply.getApplyUserId());
                    blockContact.setContactType(userContactApply.getContactType());
                    blockContact.setStatus(UserContactStatusEnum.BLOCKED_THE_FRIEND);
                    blockContact.setCreateTime(now);
                    blockContact.setLastUpdateTime(now);
                    log.info("新增拉黑关系 {}", blockContact);
                    userContactMapper.insert(blockContact);
                } else {
                    // 修改为拉黑
                    userContact.setStatus(UserContactStatusEnum.BLOCKED_THE_FRIEND);
                    userContact.setLastUpdateTime(now);
                    log.info("修改联系人状态为拉黑 {}", userContact);
                    userContactMapper.updateById(userContact);
                }
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
    }
}
