package com.r.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.AppUpdateDTO;
import com.r.chat.entity.dto.AppUpdateReleaseDTO;
import com.r.chat.entity.enums.AppUpdateMethodTypeEnum;
import com.r.chat.entity.enums.AppUpdateStatusEnum;
import com.r.chat.entity.po.AppUpdate;
import com.r.chat.entity.vo.AppUpdateVO;
import com.r.chat.exception.*;
import com.r.chat.mapper.AppUpdateMapper;
import com.r.chat.properties.AppProperties;
import com.r.chat.service.IAppUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

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
    private final AppProperties appProperties;

    private final AppUpdateMapper appUpdateMapper;

    @Override
    public void saveOrUpdateAppUpdate(AppUpdateDTO appUpdateDTO) {
        if (appUpdateDTO.getMethodType() == null) {
            log.warn("新增或修改app更新信息失败: 更新手段信息错误 {}", appUpdateDTO);
            throw new EnumIsNullException(Constants.MESSAGE_STATUS_ERROR);
        }
        if (AppUpdateMethodTypeEnum.FILE.equals(appUpdateDTO.getMethodType())) {
            // 文件更新方式需要保存文件
            if (appUpdateDTO.getFile() == null) {
                log.warn("新增或修改app更新信息失败: 未上传文件 {}", appUpdateDTO);
                throw new ParameterErrorException(Constants.MESSAGE_MISSING_FILE);
            }
            // 新增文件的操作放到最后面，因为后面可能判断出请求有错误信息，那这里就会冗余保存
            // 文件更新方式不能有外链信息
            appUpdateDTO.setOuterLink(null);
        } else if (StringUtils.isEmpty(appUpdateDTO.getOuterLink())) {
            // 外链更新方式，需要有外链信息
            log.warn("新增或修改app更新信息失败: 外链信息为空 {}", appUpdateDTO);
            throw new ParameterErrorException(Constants.MESSAGE_MISSING_OUTER_LINK);
        }
        if (appUpdateDTO.getId() == null) {
            // 新增
            if (StringUtils.isEmpty(appUpdateDTO.getVersion())) {
                log.warn("新增app更新信息失败: 版本信息为空 {}", appUpdateDTO);
                throw new ParameterErrorException(Constants.MESSAGE_MISSING_VERSION);
            }
            // 这里要求新增的版本必须比所有版本都高，这样才满足id最大的那个版本最高
            // 找到最新的版本，即id最大那个版本
            QueryWrapper<AppUpdate> latestWrapper = new QueryWrapper<>();
            latestWrapper.lambda().orderByDesc(AppUpdate::getId).last("limit 1");
            AppUpdate latest = appUpdateMapper.selectOne(latestWrapper);
            if (latest != null && StringUtils.versionGTE(latest.getVersion(), appUpdateDTO.getVersion())) {
                // 数据库里的最高version >= 新增的version，不予添加
                log.warn("新增app更新信息失败: 新增的版本低于最新版本 最新版本: {}", latest.getVersion());
                throw new AppVersionLTLatestException(Constants.MESSAGE_VERSION_TOO_LOW);
            }
            AppUpdate appUpdate = CopyUtils.copyBean(appUpdateDTO, AppUpdate.class);
            appUpdate.setCreateTime(LocalDateTime.now());
            appUpdate.setStatus(AppUpdateStatusEnum.UNPUBLISHED);
            save(appUpdate);
            log.info("新增app更新信息成功 {}", appUpdate);
            if (AppUpdateMethodTypeEnum.FILE.equals(appUpdateDTO.getMethodType())) {
                // 保存文件，版本从传入的数据中获取
                FileUtils.saveExeFile(appUpdateDTO.getFile(), appUpdateDTO.getVersion());
            }
        } else {
            // 修改
            // 查找原来的信息
            AppUpdate origin = appUpdateMapper.selectById(appUpdateDTO.getId());
            if (origin == null) {
                log.warn("修改app更新信息失败: 原app更新信息不存在 {}", appUpdateDTO);
                throw new AppUpdateNotExistException(Constants.MESSAGE_APP_UPDATE_NOT_EXIST);
            }
            // 不允许修改版本号，否则无法保证id越大版本越高（理论上也可以保证，不过要加很多判断逻辑，且会有隐患），整个更新系统会乱套
            if (appUpdateDTO.getVersion() != null) {
                log.warn("修改app更新信息失败: 不允许修改版本号 {}", appUpdateDTO);
                throw new IllegalOperationException(Constants.MESSAGE_CANNOT_CHANGE_VERSION);
            }
            // 拷贝要修改的信息
            AppUpdate update = CopyUtils.copyBean(appUpdateDTO, AppUpdate.class);
            update.setId(appUpdateDTO.getId());
            // 更新信息
            updateById(update);
            log.info("修改app更新信息成功 {}", appUpdateDTO);
            if (AppUpdateMethodTypeEnum.FILE.equals(appUpdateDTO.getMethodType())) {
                // 保存文件，版本从原数据中获取
                FileUtils.saveExeFile(appUpdateDTO.getFile(), origin.getVersion());
            }
        }
    }

    @Override
    public void releaseUpdate(AppUpdateReleaseDTO appUpdateReleaseDTO) {
        AppUpdateStatusEnum status = appUpdateReleaseDTO.getStatus();
        if (status == null) {
            log.warn("发布app更新失败: 状态信息为空 {}", appUpdateReleaseDTO);
            throw new EnumIsNullException(Constants.MESSAGE_STATUS_ERROR);
        }
        // 查找原来的app更新信息
        AppUpdate dbInfo = appUpdateMapper.selectById(appUpdateReleaseDTO.getId());
        if (dbInfo == null) {
            log.warn("发布app更新失败: 信息不存在 {}", appUpdateReleaseDTO);
            throw new AppUpdateNotExistException(Constants.MESSAGE_APP_UPDATE_NOT_EXIST);
        }
        if (AppUpdateStatusEnum.GRAYSCALE_RELEASE.equals(status) && StringUtils.isEmpty(appUpdateReleaseDTO.getGrayscaleIds())) {
            log.warn("发布app更新失败: 选择灰度发布而灰度用户列表为空 {}", appUpdateReleaseDTO);
            throw new ParameterErrorException(Constants.MESSAGE_MISSING_GRAYSCALE_IDS);
        }
        if (!AppUpdateStatusEnum.GRAYSCALE_RELEASE.equals(status)) {
            // 除了灰度发布，取消发布和全网发布都不需要保存灰度用户列表
            // 已设置不检查grayscaleIds字段的值，直接更新，所以置为null就可以。
            // 在灰度更新改为全网更新的时候也可以将该字段置为null
            appUpdateReleaseDTO.setGrayscaleIds(null);
        }
        AppUpdate appUpdate = CopyUtils.copyBean(appUpdateReleaseDTO, AppUpdate.class);
        updateById(appUpdate);
        switch (status) {
            case UNPUBLISHED:
                log.info("取消发布app更新 {}", appUpdateReleaseDTO);
                break;
            case GRAYSCALE_RELEASE:
                log.info("灰度发布app更新 {}", appUpdateReleaseDTO);
                break;
            case FULL_RELEASE:
                log.info("全网发布app更新 {}", appUpdateReleaseDTO);
                break;
            default:
                log.warn(Constants.IN_SWITCH_DEFAULT);
                throw new ParameterErrorException(Constants.IN_SWITCH_DEFAULT);
        }
    }

    @Override
    public void delUpdate(Integer id) {
        AppUpdate appUpdate = getById(id);
        if (appUpdate == null) {
            log.warn("删除app更新信息失败: 信息不存在 id: {}", id);
            throw new AppUpdateNotExistException(Constants.MESSAGE_APP_UPDATE_NOT_EXIST);
        }
        // 发布的更新不能删除
        if (!AppUpdateStatusEnum.UNPUBLISHED.equals(appUpdate.getStatus())) {
            log.warn("删除app更新信息失败: 更新已经发布 {}", appUpdate);
            throw new IllegalOperationException(Constants.MESSAGE_APP_ALREADY_RELEASED);
        }
        removeById(id);
        log.info("删除app更新信息成功 {}", appUpdate);
    }

    @Override
    public AppUpdateVO checkVersion(String version) {
        // 需要根据用户是否是灰度用户来寻找适合该用户的最新版本
        AppUpdate latest = appUpdateMapper.selectLatestForUser(UserIdContext.getCurrentUserId());
        if (latest == null || StringUtils.versionGTE(version, latest.getVersion())) {
            // 无最新版本或当前版本等于最新版本，无需更新版本
            log.info("当前已是最新版本, 无需更新");
            return null;
        }
        AppUpdateVO appUpdateVO = CopyUtils.copyBean(latest, AppUpdateVO.class);
        // 外链直接返回
        if (AppUpdateMethodTypeEnum.OUTER_LINK.equals(appUpdateVO.getMethodType())) {
            log.info("获取到最新版本 {}", appUpdateVO);
            return appUpdateVO;
        }
        // 如果是文件下载方式，需要补充文件名和文件大小
        File file = FileUtils.getExeFile(latest.getVersion());
        appUpdateVO.setFileName(appProperties.getAppName() + "_" + file.getName());
        appUpdateVO.setSize(file.length());
        log.info("获取到最新版本 {}", appUpdateVO);
        return appUpdateVO;
    }
}
