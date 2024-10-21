package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.context.AdminContext;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.GroupInfoQueryDTO;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.GroupInfo;
import com.r.chat.entity.po.UserContact;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.exception.GroupCountLimitException;
import com.r.chat.exception.GroupNotExistException;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;

/**
 * <p>
 * 群聊信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IGroupInfoService {
    private final RedisUtils redisUtils;
    private final UserContactMapper userContactMapper;
    private final GroupInfoMapper groupInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateGroupInfo(GroupInfoDTO groupInfoDTO) {
        LocalDateTime now = LocalDateTime.now();  // 当前时间
        // 填充GroupInfo对象
        GroupInfo groupInfo = CopyUtils.copyBean(groupInfoDTO, GroupInfo.class);
        if (StringUtils.isEmpty(groupInfoDTO.getGroupId())) {
            // 新增群聊
            // 判断该用户的群聊是否已经达到上限
            SysSettingDTO sysSettingDTO = redisUtils.getSysSetting();
            Long count = lambdaQuery().eq(GroupInfo::getGroupOwnerId, UserIdContext.getCurrentUserId()).count();
            if (count > sysSettingDTO.getMaxGroupCount()) {
                log.warn("拒绝新增群聊: 群聊数量达到上限 [{}]", sysSettingDTO.getMaxGroupCount());
                throw new GroupCountLimitException(String.format(Constants.MESSAGE_GROUP_COUNT_LIMIT, sysSettingDTO.getMaxGroupCount()));
            }

            // todo 暂时注释掉这段代码，让前端先跳过上传图片的逻辑，后续需要再放开
//            // 没有携带群头像
//            if (groupInfoDTO.getAvatarFile() == null) {
//                log.warn("拒绝新增群聊: 未指定群头像");
//                throw new ParameterErrorException(Constants.MESSAGE_MISSING_FILE);
//            }

            // 添加群聊到数据库
            // 新建群号，补充内容
            groupInfo.setGroupId(StringUtils.getRandomGroupId());
            groupInfo.setGroupOwnerId(UserIdContext.getCurrentUserId());
            groupInfo.setCreateTime(now);
            groupInfo.setStatus(GroupInfoStatusEnum.NORMAL);
            save(groupInfo);

            // 将自己加入群聊（添加联系人信息）
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setContactType(UserContactTypeEnum.GROUP);
            userContact.setStatus(UserContactStatusEnum.FRIENDS);
            userContact.setCreateTime(now);
            userContact.setLastUpdateTime(now);
            userContactMapper.insert(userContact);
            log.info("新增群聊成功 {}", groupInfo);
        } else {
            // 修改群聊信息
            updateById(groupInfo);
            log.info("修改群聊信息成功 {}", groupInfo);
        }
        // 头像文件的操作
        if (groupInfoDTO.getAvatarFile() == null) {
            return;
        }
        // 头像保存到本地
        FileUtils.saveAvatarFile(groupInfoDTO);
    }

    @Override
    public Page<GroupDetailInfoVO> loadGroupDetailInfo(Page<GroupDetailInfoVO> page, GroupInfoQueryDTO groupInfoQueryDTO) {
        // 前端发送的id是不带前缀的，如果传递了id的查询条件的话需要补充前缀
        if (!StringUtils.isEmpty(groupInfoQueryDTO.getGroupId())) {
            // 补充groupId的前缀
            groupInfoQueryDTO.setGroupId(IdPrefixEnum.GROUP.getPrefix() + groupInfoQueryDTO.getGroupId());
        }
        if (!StringUtils.isEmpty(groupInfoQueryDTO.getGroupOwnerId())) {
            // 补充群主id的前缀
            groupInfoQueryDTO.setGroupOwnerId(IdPrefixEnum.USER.getPrefix() + groupInfoQueryDTO.getGroupOwnerId());
        }
        return groupInfoMapper.selectGroupDetailInfoPage(page, groupInfoQueryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupId) {
        GroupInfo groupInfo = lambdaQuery().eq(GroupInfo::getGroupId, groupId).one();
        if (groupInfo == null) {
            log.warn("解散群聊失败: 群聊不存在 groupId: {}", groupId);
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        // 有两种情况可以解散群聊：1、群主本人解散 2、管理员强制解散
        if (!groupInfo.getGroupOwnerId().equals(UserIdContext.getCurrentUserId()) || !AdminContext.isAdmin()) {
            log.warn("解散群聊失败: 非群主或管理员操作 操作人id: {}, groupId: {}, 群主id: {}", UserIdContext.getCurrentUserId(), groupId, groupInfo.getGroupOwnerId());
            throw new IllegalOperationException(Constants.MESSAGE_ILLEGAL_OPERATION);
        }
        // 更新群聊状态为解散
        groupInfo.setStatus(GroupInfoStatusEnum.DISBAND);
        updateById(groupInfo);
        // 将所有群成员对群聊的关系设置为被删除
        UpdateWrapper<UserContact> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserContact::getContactId, groupInfo.getGroupId())
                .eq(UserContact::getContactType, UserContactTypeEnum.GROUP)
                .set(UserContact::getStatus, UserContactStatusEnum.DELETED_BY_FRIEND)
                .set(UserContact::getLastUpdateTime, LocalDateTime.now());
        userContactMapper.update(null, updateWrapper);
        log.info("解散群聊成功 groupId: {}", groupId);

        // todo 移除群成员的联系人缓存
        // todo 发送群聊被解散的信息
    }
}
