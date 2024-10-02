package com.r.chat.service.impl;

import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.po.GroupInfo;
import com.r.chat.entity.po.UserContact;
import com.r.chat.exception.FileSaveFailedException;
import com.r.chat.exception.GroupCountLimitException;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.exception.MissingRequestParametersException;
import com.r.chat.mapper.GroupInfoMapper;
import com.r.chat.mapper.UserContactMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.utils.VerifyUtils;
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
    private final AppProperties appProperties;

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
            Long count = lambdaQuery().eq(GroupInfo::getGroupOwnerId, groupInfoDTO.getGroupOwnerId()).count();
            if (count > sysSettingDTO.getMaxGroupCount()) {
                log.warn("拒绝新增群聊: 群聊数量达到上限 [{}]", sysSettingDTO.getMaxGroupCount());
                throw new GroupCountLimitException(Constants.MESSAGE_GROUP_COUNT_LIMIT + ": [" + sysSettingDTO.getMaxGroupCount() + "]");
            }

            // 没有携带群头像
            if (groupInfoDTO.getAvatarFile() == null) {
                log.warn("拒绝新增群聊: 未指定群头像");
                throw new MissingRequestParametersException(Constants.MESSAGE_MISSING_AVATAR_FILE);
            }

            // 添加群聊到数据库
            // 新建群号，设置添加时间
            groupInfo.setGroupId(StringUtils.getRandomGroupId());
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
            log.info("新增群聊成功: {}", groupInfo);
        } else {
            // 修改群聊信息
            // 需要断言当前的用户就是群主
            VerifyUtils.assertIsCurrentUser(groupInfoDTO.getGroupOwnerId());
            updateById(groupInfo);
            log.info("修改群聊信息成功: {}", groupInfo);
        }
        // 头像文件的操作
        if (groupInfoDTO.getAvatarFile() == null) {
            return;
        }
        // 保存到本地
        String baseFolder = appProperties.getProjectFolder();
        File targetFolder = new File(baseFolder, Constants.FILE_FOLDER_AVATAR);
        if (!targetFolder.exists()) {
            if (targetFolder.mkdirs()) {
                log.debug("创建目录: {}", targetFolder.getAbsolutePath());
            } else {
                log.warn("创建目录失败: {}", targetFolder.getAbsolutePath());
            }
        }
        // 使用群号当文件名
        try {
            File avatarFile = new File(targetFolder, groupInfo.getGroupId() + Constants.FILE_SUFFIX_AVATAR);
            File coverFile = new File(targetFolder, groupInfo.getGroupId() + Constants.FILE_SUFFIX_COVER);
            groupInfoDTO.getAvatarFile().transferTo(avatarFile);
            log.info("保存图片文件: {}", avatarFile.getAbsolutePath());
            groupInfoDTO.getAvatarCover().transferTo(coverFile);
            log.info("保存图片文件: {}", coverFile.getAbsolutePath());
        } catch (Exception e) {
            // 保存文件失败
            log.error("头像文件保存失败: {}", e.getMessage());
            throw new FileSaveFailedException(Constants.MESSAGE_FAILED_TO_SAVE_AVATAR_FILE);
        }
    }
}
