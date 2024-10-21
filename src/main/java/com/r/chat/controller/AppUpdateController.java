package com.r.chat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.dto.AppUpdateDTO;
import com.r.chat.entity.dto.AppUpdateQueryDTO;
import com.r.chat.entity.po.AppUpdate;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IAppUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
@Validated
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

    /**
     * 新增或更新app更新信息
     */
    @PostMapping("/saveUpdate")
    public Result<String> saveUpdate(@Valid AppUpdateDTO appUpdateDTO) {
        log.info("新增或修改app更新信息 {}", appUpdateDTO);
        appUpdateService.saveOrUpdateAppUpdate(appUpdateDTO);
        return Result.success();
    }
}
