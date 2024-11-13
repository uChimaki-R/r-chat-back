package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.*;
import com.r.chat.entity.enums.*;
import com.r.chat.entity.notice.ContactRenameNotice;
import com.r.chat.entity.notice.ForceOfflineNotice;
import com.r.chat.entity.po.*;
import com.r.chat.exception.*;
import com.r.chat.mapper.*;
import com.r.chat.properties.AppProperties;
import com.r.chat.properties.DefaultSysSettingProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserContactService;
import com.r.chat.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import com.r.chat.websocket.utils.ChannelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
    private final IUserContactService userContactService;

    private final UserInfoMapper userInfoMapper;
    private final UserContactMapper userContactMapper;
    private final BeautyUserInfoMapper beautyUserInfoMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;

    private final AppProperties appProperties;
    private final RedisUtils redisUtils;
    private final DefaultSysSettingProperties defaultSysSettingProperties;
    private final ChannelUtils channelUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)  // 多次数据库操作，需要事务管理
    public void register(RegisterDTO registerDTO) {
        // 注册账号
        // 检查邮箱是否已经注册
        UserInfo userInfo = lambdaQuery()
                .eq(UserInfo::getEmail, registerDTO.getEmail())
                .one();
        if (userInfo != null) {
            log.warn("拒绝注册: 邮箱 [{}] 已存在", userInfo.getEmail());
            throw new EmailAlreadyRegisteredException(Constants.MESSAGE_EMAIL_ALREADY_REGISTERED);
        }
        // 随机获取一个userId
        String userId = StringUtils.getRandomUserId();
        // 判断是否可以使用靓号注册，可以的话需要替换为靓号
        BeautyUserInfo beautyUserInfo = beautyUserInfoMapper.selectOne(new QueryWrapper<BeautyUserInfo>().lambda()
                .eq(BeautyUserInfo::getEmail, registerDTO.getEmail()));
        if (null != beautyUserInfo && beautyUserInfo.getStatus() != BeautyUserInfoStatusEnum.USED) {
            // 换成靓号
            userId = beautyUserInfo.getUserId();
            log.info("该注册邮箱可以获得靓号: {}", userId);
            // 修改靓号为已使用
            beautyUserInfo.setStatus(BeautyUserInfoStatusEnum.USED);
            beautyUserInfoMapper.updateById(beautyUserInfo);
        }
        LocalDateTime now = LocalDateTime.now();
        Long millis = System.currentTimeMillis();
        userInfo = CopyUtils.copyBean(registerDTO, UserInfo.class);
        userInfo.setUserId(userId);
        userInfo.setPassword(StringUtils.encodeMd5(registerDTO.getPassword())); // 使用md5加密后再存储
        userInfo.setStatus(UserStatusEnum.ENABLE);
        userInfo.setCreateTime(now);
        userInfo.setLastOffTime(millis);
        userInfoMapper.insert(userInfo);
        log.info("新增用户信息 {}", userInfo);

        // 添加机器人为好友
        String robotId = defaultSysSettingProperties.getRobotId();
        ContactApplyAddDTO robotAdd = new ContactApplyAddDTO();
        robotAdd.setApplyUserId(robotId);  // 机器人的欢迎消息等价于其他用户添加别人时的申请信息，所以申请人是机器人，接收者是新用户
        robotAdd.setContactId(userId);
        robotAdd.setContactType(UserContactTypeEnum.USER);
        userContactService.addContact(robotAdd);
        log.info("{} 成功添加机器人为好友", userId);

        log.info("注册新账号成功 {}", userInfo);
    }

    @Override
    public UserTokenInfoDTO login(LoginDTO loginDTO) {
        // 登录账号
        // 检查邮箱是否存在
        UserInfo userInfo = lambdaQuery()
                .eq(UserInfo::getEmail, loginDTO.getEmail())
                .one();
        if (userInfo == null) {
            log.warn("拒绝登录: 账号 [{}] 不存在", loginDTO.getEmail());
            throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        // 校验密码是否正确
        if (!userInfo.getPassword().equals(loginDTO.getPassword())) { // 登陆时前端传来的密码是密文，和数据库中的比对前就不需要再加密了
            log.warn("拒绝登录: 账号 [{}] 密码错误", loginDTO.getEmail());
            throw new PasswordErrorException(Constants.MESSAGE_PASSWORD_ERROR);
        }
        // 检测账号是否已经登录
        if (redisUtils.getUserHeartBeat(userInfo.getUserId()) != null) {
            log.warn("拒绝登录: 账号 [{}] 已在别处登录", loginDTO.getEmail());
            throw new UserAlreadyLoginException(Constants.MESSAGE_ACCOUNT_ALREADY_LOGIN);
        }
        // 检测账号是否被禁用
        if (UserStatusEnum.DISABLED == userInfo.getStatus()) {
            log.warn("拒绝登录: 账号 [{}] 已被锁定", loginDTO.getEmail());
            throw new UserDisableException(Constants.MESSAGE_ACCOUNT_DISABLE);
        }
        UserTokenInfoDTO userTokenInfoDTO = CopyUtils.copyBean(userInfo, UserTokenInfoDTO.class);
        // 查看是否管理员账号
        boolean isAdmin = appProperties.getAdminUserIds().contains(userInfo.getUserId());
        userTokenInfoDTO.setAdmin(isAdmin);
        // 设置并保存token
        String token = StringUtils.generateToken(userInfo.getUserId());
        userTokenInfoDTO.setToken(token);
        // 保存用户token到userTokenInfo的映射到redis，以后登录就可以从上下文里获取用户id
        // 而且后面聊天的时候需要获取用户名，应该使用redis的而不是每次发送消息都查一遍数据库占用性能
        // 再者userTokenInfoDTO里面保存了token信息，也就是说即使在业务层，不仅能获取到userId，还能获取到token
        // 能获取到token，就能够根据这个token更新缓存的userTokenInfo信息（主要是更新用户名）
        redisUtils.setToken2UserTokenInfo(token, userTokenInfoDTO);
        log.info("{}账号 [{}] 登录成功", isAdmin ? "管理员" : "", loginDTO.getEmail());

        // 查询联系人信息并保存到redis中，供后续netty聊天功能使用
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserContact::getUserId, userInfo.getUserId())
                .eq(UserContact::getStatus, UserContactStatusEnum.FRIENDS);
        List<UserContact> contacts = userContactMapper.selectList(queryWrapper);
        // 清空原有的id列表
        redisUtils.removeUserContactIds(userInfo.getUserId());
        if (contacts != null && !contacts.isEmpty()) {
            // 只取联系人的id
            List<String> contactIds = contacts.stream().map(UserContact::getContactId).collect(Collectors.toList());
            // 保存新的id列表到redis
            redisUtils.setContactIds(userInfo.getUserId(), contactIds);
            log.info("保存用户联系人id列表到redis userId: {}, {}", userInfo.getUserId(), contactIds);
        }

        return userTokenInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        String userId = UserTokenInfoContext.getCurrentUserId();
        String newContactName = userInfoDTO.getNickName();
        // 查看是否更新了名字，更新了的话需要修改会话中的昵称
        UserInfo userInfo = getById(userId);
        if (!userInfo.getNickName().equals(newContactName)) {
            // 名字不一样，需要修改会话中的名称，并发送通知让前端重新渲染
            log.info("修改了用户名称, 需要修改会话中的用户名称为: {}", newContactName);
            UpdateWrapper<ChatSessionUser> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ChatSessionUser::getContactId, userInfo.getUserId())
                    .set(ChatSessionUser::getContactName, newContactName);
            chatSessionUserMapper.update(null, updateWrapper);
            log.info("更新会话中的用户名称信息成功 newContactName: {}", newContactName);
            // 发送用户名修改的通知给所有该用户的好友
            // 查找自己的好友
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(UserContact::getContactId, userInfo.getUserId())  // 联系人是自己
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIENDS);
            userContactMapper.selectList(queryWrapper).stream().map(UserContact::getUserId).forEach(receiveId -> { // 获取每个的userId（contactId是自己）
                // 对每个自己的好友发送消息
                ContactRenameNotice contactRenameNotice = new ContactRenameNotice();
                contactRenameNotice.setReceiveId(receiveId);  // 发给好友
                contactRenameNotice.setContactId(userId);  // 改了名字的是自己
                contactRenameNotice.setContactName(newContactName);  // 自己的新名字
                channelUtils.sendNotice(contactRenameNotice);
                log.info("发送用户名称修改的ws通知给好友 {} {}", receiveId, contactRenameNotice);
            });
            // 更新redis中userTokenInfo中的名字
            // 获取原来的来修改
            UserTokenInfoDTO currentUserTokenInfo = UserTokenInfoContext.getCurrentUserTokenInfo();
            if (currentUserTokenInfo != null) {
                // 修改名字
                currentUserTokenInfo.setNickName(newContactName);
                redisUtils.setToken2UserTokenInfo(currentUserTokenInfo.getToken(), currentUserTokenInfo);
                log.info("更新redis中的用户昵称成功 {}", currentUserTokenInfo);
            }
        }

        // 更新数据
        UserInfo updateInfo = CopyUtils.copyBean(userInfoDTO, UserInfo.class);
        updateInfo.setUserId(userId);
        // 对UserInfo已经设置了为null/空字符不更新的注解，直接更新就行了
        updateById(updateInfo);
        log.info("更新用户信息成功");
        // 头像保存到本地
        FileUtils.saveAvatarFile(userInfoDTO);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        // 检查旧密码是否正确
        UserInfo userInfo = getById(UserTokenInfoContext.getCurrentUserId());
        if (!userInfo.getPassword().equals(StringUtils.encodeMd5(passwordUpdateDTO.getOldPassword()))) {
            log.warn("更新密码失败: 密码错误");
            throw new PasswordErrorException(Constants.MESSAGE_PASSWORD_ERROR);
        }
        // 更新密码
        String newPassword = StringUtils.encodeMd5(passwordUpdateDTO.getNewPassword());
        userInfo.setPassword(newPassword);
        updateById(userInfo);
        log.info("更新密码成功 md5password: {}", newPassword);

        // 关闭ws连接，前端会强制退出，要求重新登陆
        channelUtils.removeChannel(UserTokenInfoContext.getCurrentUserId());
    }

    @Override
    public void logout() {
        // 移除用户登录token
        redisUtils.removeUserTokenInfoByToken(UserTokenInfoContext.getCurrentUserToken());
        log.info("移除登录token");

        // 清空id列表
        redisUtils.removeUserContactIds(UserTokenInfoContext.getCurrentUserId());
        log.info("清空联系人id列表");

        // 关闭ws连接
        channelUtils.removeChannel(UserTokenInfoContext.getCurrentUserId());
    }

    @Override
    public void updateUserStatus(UserStatusDTO userStatusDTO) {
        // status已用valid验证，只用验证用户是否存在
        UserInfo userInfo = getById(userStatusDTO.getUserId());
        if (userInfo == null) {
            log.warn("更新用户状态失败 用户不存在");
            throw new UserNotExistException(Constants.MESSAGE_USER_NOT_EXIST);
        }
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserInfo::getUserId, userStatusDTO.getUserId())
                .set(UserInfo::getStatus, userStatusDTO.getStatus());
        update(updateWrapper);
        log.info("更新用户状态成功 {}", userStatusDTO);
    }

    @Override
    public void forceOffLine(String userId) {
        // 清空id列表
        redisUtils.removeUserContactIds(userId);
        log.info("清空用户联系人id列表 userId: {}", userId);

        // 发送消息通知用户被强制下线
        ForceOfflineNotice notice = new ForceOfflineNotice();
        notice.setReceiveId(userId);
        channelUtils.sendNotice(notice);
        log.info("发送被管理员强制下线的ws通知 {}", notice);
    }
}
