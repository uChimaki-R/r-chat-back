package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.LoginDTO;
import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.entity.dto.UserInfoDTO;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.entity.enums.UserInfoBeautyStatusEnum;
import com.r.chat.entity.enums.UserInfoStatusEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.UserInfoBeauty;
import com.r.chat.exception.*;
import com.r.chat.mapper.UserInfoBeautyMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;

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
    private final UserInfoMapper userInfoMapper;
    private final UserInfoBeautyMapper userInfoBeautyMapper;
    private final AppProperties appProperties;
    private final RedisUtils redisUtils;

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
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectOne(new QueryWrapper<UserInfoBeauty>().lambda()
                .eq(UserInfoBeauty::getEmail, registerDTO.getEmail()));
        if (null != userInfoBeauty && userInfoBeauty.getStatus() != UserInfoBeautyStatusEnum.USED) {
            // 换成靓号
            userId = userInfoBeauty.getUserId();
            // 修改靓号为已使用
            userInfoBeauty.setStatus(UserInfoBeautyStatusEnum.USED);
            userInfoBeautyMapper.updateById(userInfoBeauty);
        }
        LocalDateTime now = LocalDateTime.now();
        userInfo = CopyUtils.copyBean(registerDTO, UserInfo.class);
        userInfo.setUserId(userId);
        userInfo.setPassword(StringUtils.encodeMd5(registerDTO.getPassword())); // 使用md5加密后再存储
        userInfo.setStatus(UserInfoStatusEnum.ENABLE);
        userInfo.setCreateTime(now);
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.insert(userInfo);
        log.info("注册新账号: {}", userInfo);
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
        if (UserInfoStatusEnum.DISABLED == userInfo.getStatus()) {
            log.warn("拒绝登录: 账号 [{}] 已被锁定", loginDTO.getEmail());
            throw new UserDisableException(Constants.MESSAGE_ACCOUNT_DISABLE);
        }
        UserTokenInfoDTO userTokenInfoDTO = CopyUtils.copyBean(userInfo, UserTokenInfoDTO.class);
        // 查看是否管理员账号
        boolean isAdmin = appProperties.getAdminEmails().contains(userInfo.getEmail());
        userTokenInfoDTO.setAdmin(isAdmin);
        // 设置并保存token
        String token = StringUtils.generateToken(userInfo.getUserId());
        userTokenInfoDTO.setToken(token);
        // 保存用户token到id的映射到redis，以后登录就可以从上下文里获取用户id
        redisUtils.saveToken2UserId(token, userInfo.getUserId());
        log.info("{}账号 [{}] 登录成功", isAdmin ? "管理员" : "", loginDTO.getEmail());
        return userTokenInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        String userId = UserIdContext.getCurrentUserId();
        String newContactName = userInfoDTO.getNickName();
        // 查看是否更新了名字，更新了的话需要修改会话中的昵称
        UserInfo userInfo = getById(userId);
        if (userInfo.getNickName().equals(newContactName)) {
            // 名字一样，不用更新
            newContactName = null;
        }
        // todo 修改会话中的昵称
        // 更新数据
        UserInfo updateInfo = CopyUtils.copyBean(userInfoDTO, UserInfo.class);
        updateInfo.setUserId(userId);
        // 对UserInfo已经设置了为null/空字符不更新的注解，直接更新就行了
        updateById(updateInfo);
        log.info("更新用户信息成功");
        // 头像文件的操作
        if (userInfoDTO.getAvatarFile() == null) {
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
        // 使用用户id组成文件名
        try {
            File avatarFile = new File(targetFolder, userId + Constants.FILE_SUFFIX_AVATAR);
            File coverFile = new File(targetFolder, userId + Constants.FILE_SUFFIX_COVER);
            userInfoDTO.getAvatarFile().transferTo(avatarFile);
            log.info("保存图片文件: {}", avatarFile.getAbsolutePath());
            userInfoDTO.getAvatarCover().transferTo(coverFile);
            log.info("保存图片文件: {}", coverFile.getAbsolutePath());
        } catch (Exception e) {
            // 保存文件失败
            log.error("头像文件保存失败: {}", e.getMessage());
            throw new FileSaveFailedException(Constants.MESSAGE_FAILED_TO_SAVE_AVATAR_FILE);
        }
    }
}
