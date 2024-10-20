package com.r.chat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.dto.AppUpdateQueryDTO;
import com.r.chat.entity.po.AppUpdate;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IAppUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * app更新信息 前端控制器
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-20
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/app")
public class AppUpdateController {
    private final IAppUpdateService appUpdateService;

    /**
     * 加载app更新信息列表
     */
    @GetMapping("/loadUpdateList")
    public Result<PageResult<AppUpdate>> loadUpdateList(AppUpdateQueryDTO appUpdateQueryDTO,
                                                        @RequestParam(defaultValue = "1") Long pageNo,
                                                        @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取app版本信息 pageNo: {}, pageSize: {}, {}", pageNo, pageSize, appUpdateQueryDTO);
        Page<AppUpdate> page = appUpdateService.lambdaQuery()
                .ge(appUpdateQueryDTO.getStartTime() != null, AppUpdate::getCreateTime, appUpdateQueryDTO.getStartTime())
                .le(appUpdateQueryDTO.getEndTime() != null, AppUpdate::getCreateTime, appUpdateQueryDTO.getEndTime())
                .orderByDesc(AppUpdate::getCreateTime)
                .page(new Page<>(pageNo, pageSize));
        PageResult<AppUpdate> pageResult = PageResult.fromPage(page);
        log.info("获取到app版本信息 {}", pageResult);
        return Result.success(pageResult);
    }
}
