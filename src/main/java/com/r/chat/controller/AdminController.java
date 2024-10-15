package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.po.UserInfo;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<PageResult<UserInfo>> loadUser(Long pageNo, Long pageSize) {
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? Constants.SIZE_DEFAULT_PAGE_SIZE : pageSize;
        log.info("获取用户信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        Page<UserInfo> page = userInfoService.lambdaQuery()
                .orderBy(true, true, UserInfo::getCreateTime)
                .page(new Page<>(pageNo, pageSize));
        PageResult<UserInfo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo);
        pageResult.setPageSize(pageSize);
        pageResult.setPageTotal(page.getPages());
        pageResult.setTotalCount(page.getTotal());
        pageResult.setData(page.getRecords());
        log.info("获取到用户信息 {}", pageResult);
        return Result.success(pageResult);
    }
}
