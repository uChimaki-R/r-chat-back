package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.UserInfoQueryDTO;
import com.r.chat.entity.dto.UserStatusDTO;
import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IUserInfoService;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {
    private final IUserInfoService userInfoService;

    /**
     * 加载用户的信息
     */
    @GetMapping("/loadUser")
    public Result<PageResult<UserInfo>> loadUser(UserInfoQueryDTO userInfoQueryDTO,
                                                 @RequestParam(defaultValue = "1") Long pageNo,
                                                 @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取用户信息 pageNo: {}, pageSize: {}, {}", pageNo, pageSize, userInfoQueryDTO);
        Page<UserInfo> page = userInfoService.lambdaQuery()
                // 下面两个是可以选择传递的查询条件
                .eq(!StringUtils.isEmpty(userInfoQueryDTO.getUserId()), UserInfo::getUserId, IdPrefixEnum.USER.getPrefix() + userInfoQueryDTO.getUserId())  // 前端传递的只有数字，需要加上用户id前缀
                .like(!StringUtils.isEmpty(userInfoQueryDTO.getNickName()), UserInfo::getNickName, userInfoQueryDTO.getNickName())
                .orderByDesc(UserInfo::getCreateTime)
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
