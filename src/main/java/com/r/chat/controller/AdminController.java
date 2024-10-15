package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.UserStatusDTO;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.UserInfoBeauty;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IUserInfoBeautyService;
import com.r.chat.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IUserInfoService userInfoService;
    private final IUserInfoBeautyService userInfoBeautyService;

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
     * 加载靓号信息
     */
    @GetMapping("/loadBeautyAccountList")
    public Result<PageResult<UserInfoBeauty>> loadBeautyAccountList(@RequestParam(defaultValue = "1") Long pageNo,
                                                                    @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取靓号信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        Page<UserInfoBeauty> page = userInfoBeautyService.lambdaQuery()
                .orderBy(true, true, UserInfoBeauty::getId)
                .page(new Page<>(pageNo, pageSize));
        PageResult<UserInfoBeauty> pageResult = PageResult.fromPage(page);
        log.info("获取到靓号信息 {}", pageResult);
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

    /**
     * 强制用户下线
     */
    @DeleteMapping("/forceOffLine")
    public Result<String> forceOffLine(@NotEmpty(message = Constants.VALIDATE_EMPTY_USER_ID) String userId) {
        log.info("强制下线 userId: {}", userId);
        userInfoService.forceOffLine(userId);
        return Result.success();
    }
}
