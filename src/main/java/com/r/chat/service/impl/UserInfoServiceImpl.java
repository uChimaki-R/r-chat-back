package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.LoginDTO;
import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.entity.vo.UserInfoVO;
import com.r.chat.entity.enums.UserInfoBeautyStatusEnum;
import com.r.chat.entity.enums.UserInfoStatusEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.UserInfoBeauty;
import com.r.chat.exception.BusinessException;
import com.r.chat.mapper.UserInfoBeautyMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.MyStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.warn("拒绝注册：邮箱 [{}] 已存在", userInfo.getEmail());
            throw new BusinessException("邮箱已注册");
        }
        // 新增用户
        userInfo = new UserInfo();
        // 随机获取一个userId
        String userId = MyStringUtils.getRandomUserId();
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
        BeanUtils.copyProperties(registerDTO, userInfo);
        userInfo.setUserId(userId);
        userInfo.setPassword(MyStringUtils.encodeMd5(registerDTO.getPassword())); // 使用md5加密后再存储
        userInfo.setStatus(UserInfoStatusEnum.ENABLE);
        userInfo.setCreateTime(now);
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.insert(userInfo);
        log.info("注册新账号：{}", userInfo);
    }

    @Override
    public UserInfoVO login(LoginDTO loginDTO) {
        // 登录账号
        // 检查邮箱是否存在
        UserInfo userInfo = lambdaQuery()
                .eq(UserInfo::getEmail, loginDTO.getEmail())
                .one();
        if (userInfo == null) {
            log.warn("拒绝登录：账号 [{}] 不存在", loginDTO.getEmail());
            throw new BusinessException("账号不存在");
        }
        // 检测账号是否被禁用
        if (UserInfoStatusEnum.DISABLED == userInfo.getStatus()) {
            log.warn("拒绝登录：账号 [{}] 已被锁定", loginDTO.getEmail());
            throw new BusinessException("账号被锁定");
        }
        // 检测账号是否已经登录
        if (redisUtils.getUserHeartBeat(userInfo.getUserId()) != null) {
            log.warn("拒绝登录：账号 [{}] 已在别处登录", loginDTO.getEmail());
            throw new BusinessException("此账号已在别处登录");
        }
        // 校验密码是否正确
        if (!userInfo.getPassword().equals(loginDTO.getPassword())) { // 登陆时前端传来的密码是密文，和数据库中的比对前就不需要再加密了
            log.warn("拒绝登录：账号 [{}] 密码错误", loginDTO.getEmail());
            throw new BusinessException("密码错误");
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);
        // 查看是否管理员账号
        boolean isAdmin = appProperties.getAdminEmails().contains(userInfo.getEmail());
        userInfoVO.setAdmin(isAdmin);
        // 设置并保存token
        String token = MyStringUtils.encodeMd5(userInfo.getUserId() + MyStringUtils.getRandomChars(Constants.LENGTH_TOKEN_RANDOM_CHARS));
        userInfoVO.setToken(token);
        // 保存到redis
        redisUtils.saveUserTokenInfo(userInfoVO);
        log.info("账号 [{}] 登录成功", loginDTO.getEmail());
        return userInfoVO;
    }
}
