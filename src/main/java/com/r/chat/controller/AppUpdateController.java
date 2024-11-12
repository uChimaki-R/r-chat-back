package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.AppUpdateVO;
import com.r.chat.exception.FileNotExistException;
import com.r.chat.service.IAppUpdateService;
import com.r.chat.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;

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

    /**
     * 下载应用更新文件
     */
    @GetMapping("/download")
    public void downloadFile(HttpServletResponse response, @NotNull(message = Constants.VALIDATE_EMPTY_VERSION) String version) {
        log.info("下载应用更新文件 version: {}", version);
        File file = FileUtils.getExeFile(version);
        if (file == null) {
            log.warn("获取下载应用更新文件失败: 文件不存在 version: {}", version);
            throw new FileNotExistException(Constants.MESSAGE_FILE_NOT_EXIST);
        }
        log.info("获取应用更新文件成功, 开始下载文件 {}", file);
        // 下载文件的操作
        FileUtils.downLoadFile(response, file);
        log.info("下载文件成功 {}", file);
    }
}
