package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.entity.enums.UserInfoBeautyStatusEnum;
import com.r.chat.entity.enums.UserInfoStatusEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.UserInfoBeauty;
import com.r.chat.exception.BusinessException;
import com.r.chat.mapper.UserInfoBeautyMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.MyStringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
    private final UserInfoMapper userInfoMapper;
    private final UserInfoBeautyMapper userInfoBeautyMapper;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 注册用户
        // 检查邮箱是否已经注册
        UserInfo userInfo = lambdaQuery()
                .eq(UserInfo::getEmail, registerDTO.getEmail())
                .one();
        if (userInfo != null) {
            throw new BusinessException("邮箱已注册");
        }

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

        // 新增用户
        LocalDateTime now = LocalDateTime.now();
        UserInfo newUser = UserInfo.builder()
                .userId(userId)
                .email(registerDTO.getEmail())
                .nickName(registerDTO.getNickName())
                .password(MyStringUtils.encodeMd5(registerDTO.getPassword())) // 使用md5加密后再存储
                .status(UserInfoStatusEnum.ENABLE)
                .createTime(now)
                .lastOffTime(System.currentTimeMillis())
                .build();
        userInfoMapper.insert(newUser);
    }
}
