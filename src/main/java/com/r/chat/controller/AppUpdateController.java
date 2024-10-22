package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.AppUpdateVO;
import com.r.chat.service.IAppUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Slf4j
@Validated
@RestController
@RequestMapping("/update")
@RequiredArgsConstructor
public class AppUpdateController {
    private final IAppUpdateService appUpdateService;

    /**
     * 检查用户版本是否需要更新，获取该用户可用的最高版本信息
     */
    @GetMapping("/checkVersion")
    public Result<AppUpdateVO> checkVersion(@NotEmpty(message = Constants.VALIDATE_EMPTY_VERSION)
                                            @Pattern(regexp = Constants.REGEX_VERSION, message = Constants.VALIDATE_ILLEGAL_VERSION)
                                            String version) {
        log.info("检查版本更新 version: {}", version);
        // 需要根据用户是否是灰度用户来寻找适合该用户的最新版本
        AppUpdateVO appUpdateVO = appUpdateService.checkVersion(version);
        return Result.success(appUpdateVO);
    }
}
