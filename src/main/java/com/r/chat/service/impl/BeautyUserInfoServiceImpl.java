package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.BeautyUserInfoDTO;
import com.r.chat.entity.enums.UserInfoBeautyStatusEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.BeautyUserInfo;
import com.r.chat.exception.BeautyUserIdAlreadyExistedException;
import com.r.chat.exception.BeautyUserInfoNotExistException;
import com.r.chat.exception.EmailAlreadyRegisteredException;
import com.r.chat.mapper.BeautyUserInfoMapper;
import com.r.chat.mapper.UserInfoMapper;
import com.r.chat.service.IBeautyUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息（靓号） 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-09-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeautyUserInfoServiceImpl extends ServiceImpl<BeautyUserInfoMapper, BeautyUserInfo> implements IBeautyUserInfoService {
    private final UserInfoMapper userInfoMapper;

    @Override
    public void saveOrUpdateBeautyAccount(BeautyUserInfoDTO beautyUserInfoDTO) {
        BeautyUserInfo beautyUserInfo;
        if (beautyUserInfoDTO.getId() == null) {
            // 新增操作
            // 查看该账号是否已经有靓号数据了，防止一个没注册的账号占多个靓号，插入多条信息
            beautyUserInfo = lambdaQuery()
                    .eq(BeautyUserInfo::getEmail, beautyUserInfoDTO.getEmail())
                    .one();
            if (beautyUserInfo != null) {
                log.info("新增靓号信息失败: 该邮箱已经绑定了靓号 {}", beautyUserInfo);
                throw new BeautyUserIdAlreadyExistedException(Constants.MESSAGE_ALREADY_HAVE_BEAUTY_ACCOUNT);
            }
        } else {
            // 修改操作
            beautyUserInfo = getById(beautyUserInfoDTO.getId());
            if (beautyUserInfo == null) {
                log.warn("修改靓号信息失败: 靓号信息不存在");
                throw new BeautyUserInfoNotExistException(Constants.MESSAGE_BEAUTY_USER_INFO_NOT_EXIST);
            }
        }
        // 中间这段逻辑相同
        // 查看靓号是否被注册
        beautyUserInfo = lambdaQuery()
                .eq(BeautyUserInfo::getUserId, beautyUserInfoDTO.getUserId())
                .one();
        // 靓号被自己注册的话就没问题，即此次为修改操作，并且靓号信息没有改动，不需要报错
        if (beautyUserInfo != null && !(beautyUserInfoDTO.getId() != null && beautyUserInfoDTO.getId().equals(beautyUserInfo.getId()))) {
            log.warn("新增/修改靓号信息失败: 靓号信息已存在 {}", beautyUserInfo);
            throw new BeautyUserIdAlreadyExistedException(Constants.MESSAGE_BEAUTY_USER_ID_ALREADY_EXISTED);
        }
        // 查看邮箱是否已注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserInfo::getEmail, beautyUserInfoDTO.getEmail());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (userInfo != null) {
            log.warn("新增/修改靓号信息失败: 邮箱已被注册, 注册信息: {}", userInfo);
            throw new EmailAlreadyRegisteredException(Constants.MESSAGE_EMAIL_ALREADY_REGISTERED);
        }
        beautyUserInfo = CopyUtils.copyBean(beautyUserInfoDTO, BeautyUserInfo.class);
        if (beautyUserInfoDTO.getId() == null) {
            // 新增
            beautyUserInfo.setStatus(UserInfoBeautyStatusEnum.NOT_USED);
            save(beautyUserInfo);
            log.info("新增靓号信息成功 {}", beautyUserInfo);
        }
        else {
            // 修改
            updateById(beautyUserInfo);
            log.info("修改靓号信息成功 {}", beautyUserInfo);
        }
    }
}
