package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.dto.UserStatusDTO;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IUserInfoService userInfoService;

    /**
     * 加载用户的信息
     */
    @GetMapping("/loadUser")
    public Result<PageResult<UserInfo>> loadUser(@RequestParam(defaultValue = "1") Long pageNo,
                                                 @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取用户信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        Page<UserInfo> page = userInfoService.lambdaQuery()
                .orderBy(true, true, UserInfo::getCreateTime)
                .page(new Page<>(pageNo, pageSize));
        PageResult<UserInfo> pageResult = PageResult.fromPage(page);
        log.info("获取到用户信息 {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/updateUserStatus")
    public Result<String> updateUserStatus(@Valid UserStatusDTO userStatusDTO) {
        log.info("更新用户状态 {}", userStatusDTO);
        userInfoService.updateUserStatus(userStatusDTO);
        return Result.success();
    }
}
