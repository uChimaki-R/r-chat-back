package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.BeautyUserInfoDTO;
import com.r.chat.entity.dto.UserStatusDTO;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.po.BeautyUserInfo;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.exception.ParameterErrorException;
import com.r.chat.service.IGroupInfoService;
import com.r.chat.service.IUserInfoBeautyService;
import com.r.chat.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IUserInfoService userInfoService;
    private final IGroupInfoService groupInfoService;
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
     * 加载群聊信息
     */
    @GetMapping("/loadGroup")
    public Result<PageResult<GroupDetailInfoVO>> loadGroup(@RequestParam(defaultValue = "1") Long pageNo,
                                                           @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取群聊信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        // 群聊信息需要联查群主名称和群聊成员数量
        Page<GroupDetailInfoVO> page = groupInfoService.loadGroupDetailInfo(new Page<>(pageNo, pageSize));
        PageResult<GroupDetailInfoVO> pageResult = PageResult.fromPage(page);
        log.info("获取到群聊信息 {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 加载靓号信息
     */
    @GetMapping("/loadBeautyUserInfoList")
    public Result<PageResult<BeautyUserInfo>> loadBeautyAccountList(@RequestParam(defaultValue = "1") Long pageNo,
                                                                    @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取靓号信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        Page<BeautyUserInfo> page = userInfoBeautyService.lambdaQuery()
                .orderBy(true, true, BeautyUserInfo::getId)
                .page(new Page<>(pageNo, pageSize));
        PageResult<BeautyUserInfo> pageResult = PageResult.fromPage(page);
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
     * 新增或更新靓号信息
     */
    @PostMapping("/saveBeautyUserInfo")
    public Result<String> saveBeautyAccount(@Valid BeautyUserInfoDTO beautyUserInfoDTO) {
        log.info("新增或更新靓号信息 {}", beautyUserInfoDTO);
        userInfoBeautyService.saveOrUpdateBeautyAccount(beautyUserInfoDTO);
        return Result.success();
    }

    /**
     * 删除靓号信息
     */
    @DeleteMapping("/delBeautyUserInfo")
    public Result<String> delBeautyAccount(@NotNull(message = Constants.VALIDATE_EMPTY_ID) Integer id) {
        log.info("删除靓号信息 id: {}", id);
        boolean isRemove = userInfoBeautyService.removeById(id);
        if (isRemove) {
            log.info("成功删除靓号信息 id: {}", id);
            return Result.success();
        } else {
            log.warn("删除靓号信息失败 id: {}", id);
            throw new ParameterErrorException(Constants.MESSAGE_PARAMETER_ERROR);
        }
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
