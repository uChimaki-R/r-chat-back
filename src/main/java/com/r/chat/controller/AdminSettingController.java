package com.r.chat.controller;

import com.r.chat.entity.dto.SysSettingDTO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.SysSettingVO;
import com.r.chat.redis.RedisUtils;
import com.r.chat.utils.CopyUtils;
import com.r.chat.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/setting")
@RequiredArgsConstructor
public class AdminSettingController {
    private final RedisUtils redisUtils;

    /**
     * 保存系统设置
     */
    @PutMapping("/saveSysSetting")
    public Result<String> saveSysSetting(SysSettingDTO sysSettingDTO) {
        log.info("保存系统设置 {}", sysSettingDTO);
        // 保存机器人头像文件
        FileUtils.saveAvatarFile(sysSettingDTO);
        // 保存配置缓存
        redisUtils.setSysSetting(sysSettingDTO);
        return Result.success();
    }

}
