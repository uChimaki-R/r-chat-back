package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.BeautyUserInfoDTO;
import com.r.chat.entity.dto.BeautyUserInfoQueryDTO;
import com.r.chat.entity.po.BeautyUserInfo;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.exception.BeautyUserInfoNotExistException;
import com.r.chat.service.IBeautyUserInfoService;
import com.r.chat.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/beauty")
@RequiredArgsConstructor
public class AdminBeautyController {
    private final IBeautyUserInfoService beautyUserInfoService;

    /**
     * 加载靓号信息
     */
    @GetMapping("/loadBeauty")
    public Result<PageResult<BeautyUserInfo>> loadBeauty(BeautyUserInfoQueryDTO beautyUserInfoQueryDTO,
                                                         @RequestParam(defaultValue = "1") Long pageNo,
                                                         @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取靓号信息 pageNo: {}, pageSize: {}, {}", pageNo, pageSize, beautyUserInfoQueryDTO);
        Page<BeautyUserInfo> page = beautyUserInfoService.lambdaQuery()
                .like(!StringUtils.isEmpty(beautyUserInfoQueryDTO.getUserId()), BeautyUserInfo::getUserId, beautyUserInfoQueryDTO.getUserId())  // 靓号前端和数据库都是没有用户前缀的
                .like(!StringUtils.isEmpty(beautyUserInfoQueryDTO.getEmail()), BeautyUserInfo::getEmail, beautyUserInfoQueryDTO.getEmail())
                .orderByDesc(BeautyUserInfo::getId)
                .page(new Page<>(pageNo, pageSize));
        PageResult<BeautyUserInfo> pageResult = PageResult.fromPage(page);
        log.info("获取到靓号信息 {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 新增或更新靓号信息
     */
    @PostMapping("/saveBeautyUserInfo")
    public Result<String> saveBeautyUserInfo(@Valid BeautyUserInfoDTO beautyUserInfoDTO) {
        log.info("新增或更新靓号信息 {}", beautyUserInfoDTO);
        beautyUserInfoService.saveOrUpdateBeautyAccount(beautyUserInfoDTO);
        return Result.success();
    }

    /**
     * 删除靓号信息
     */
    @DeleteMapping("/delBeautyUserInfo")
    public Result<String> delBeautyUserInfo(@NotNull(message = Constants.VALIDATE_EMPTY_BEAUTY_USER_INFO_ID) Integer id) {
        log.info("删除靓号信息 id: {}", id);
        if (beautyUserInfoService.removeById(id)) {
            log.info("成功删除靓号信息 id: {}", id);
        } else {
            log.warn("删除靓号信息失败: 信息不存在 id: {}", id);
            throw new BeautyUserInfoNotExistException(Constants.MESSAGE_BEAUTY_USER_INFO_NOT_EXIST);
        }
        return Result.success();
    }

}
