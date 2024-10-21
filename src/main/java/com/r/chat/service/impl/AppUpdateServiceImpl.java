package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.AppUpdateDTO;
import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import com.r.chat.entity.enums.AppUpdateStatusEnum;
import com.r.chat.entity.po.AppUpdate;
import com.r.chat.exception.AppUpdateNotExistException;
import com.r.chat.exception.AppVersionAlreadyExistedException;
import com.r.chat.exception.AppVersionLTOriginException;
import com.r.chat.exception.ParameterErrorException;
import com.r.chat.mapper.AppUpdateMapper;
import com.r.chat.service.IAppUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * app更新信息 服务实现类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate> implements IAppUpdateService {
    private final AppUpdateMapper appUpdateMapper;

    @Override
    public void saveOrUpdateAppUpdate(AppUpdateDTO appUpdateDTO) {
        if (appUpdateDTO.getMethodType() == null) {
            log.warn("新增或修改app更新信息失败: 更新手段信息错误 {}", appUpdateDTO);
            throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
        // 不能新增或修改为已经有的版本号
        QueryWrapper<AppUpdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppUpdate::getVersion, appUpdateDTO.getVersion());
        AppUpdate sameVersionDbInfo = getOne(queryWrapper);
        if (sameVersionDbInfo != null && !Objects.equals(sameVersionDbInfo.getId(), appUpdateDTO.getId())) {
            // 有相同的版本号了，并且不是修改前的自己
            log.warn("新增或修改app更新信息失败: 该版本号已存在 {}", appUpdateDTO);
            throw new AppVersionAlreadyExistedException(Constants.MESSAGE_APP_VERSION_ALREADY_EXISTED);
        }
        if (AppUpdateMethodTypeEnum.FILE.equals(appUpdateDTO.getMethodType())) {
            // 文件更新方式需要保存文件
            if (appUpdateDTO.getFile() == null) {
                log.warn("新增或修改app更新信息失败: 未上传文件 {}", appUpdateDTO);
                throw new ParameterErrorException(Constants.MESSAGE_MISSING_FILE);
            }
            FileUtils.saveExeFile(appUpdateDTO.getFile(), appUpdateDTO.getVersion());
            // 文件更新方式不能有外链信息
            appUpdateDTO.setOuterLink(null);
        } else if (appUpdateDTO.getOuterLink() == null) {
            // 外链更新方式，需要有外链信息
            log.warn("新增或修改app更新信息失败: 外链信息为空 {}", appUpdateDTO);
            throw new ParameterErrorException(Constants.MESSAGE_MISSING_OUTER_LINK);
        }
        if (appUpdateDTO.getId() == null) {
            // 新增
            AppUpdate appUpdate = CopyUtils.copyBean(appUpdateDTO, AppUpdate.class);
            appUpdate.setCreateTime(LocalDateTime.now());
            appUpdate.setStatus(AppUpdateStatusEnum.UNPUBLISHED);
            save(appUpdate);
            log.info("新增app更新信息成功 {}", appUpdate);
        } else {
            // 修改
            // 查找原来的信息
            AppUpdate origin = appUpdateMapper.selectById(appUpdateDTO.getId());
            if (origin == null) {
                log.warn("修改app更新信息失败: 原app更新信息不存在 {}", appUpdateDTO);
                throw new AppUpdateNotExistException(Constants.MESSAGE_APP_UPDATE_NOT_EXIST);
            }
            // 修改的版本号不能低于原来的版本号
            if (!StringUtils.versionGTE(appUpdateDTO.getVersion(), origin.getVersion())) {
                log.warn("修改app更新信息失败: 修改的版本号小于原来的版本号 {}", appUpdateDTO);
                throw new AppVersionLTOriginException(Constants.MESSAGE_VERSION_LESS_THAN_ORIGIN);
            }
            // 拷贝要修改的信息
            AppUpdate update = CopyUtils.copyBean(appUpdateDTO, AppUpdate.class);
            update.setId(appUpdateDTO.getId());
            // 更新信息
            updateById(update);
            log.info("修改app更新信息成功 {}", origin);
        }
    }
}
