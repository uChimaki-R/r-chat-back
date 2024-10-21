package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoQueryDTO;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.service.IGroupInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/group")
@RequiredArgsConstructor
public class AdminGroupController {
    private final IGroupInfoService groupInfoService;

    /**
     * 加载群聊信息
     */
    @GetMapping("/loadGroup")
    public Result<PageResult<GroupDetailInfoVO>> loadGroup(GroupInfoQueryDTO groupInfoQueryDTO,
                                                           @RequestParam(defaultValue = "1") Long pageNo,
                                                           @RequestParam(defaultValue = "15") Long pageSize) {
        log.info("获取群聊信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        // 群聊信息需要联查群主名称和群聊成员数量
        Page<GroupDetailInfoVO> page = groupInfoService.loadGroupDetailInfo(new Page<>(pageNo, pageSize), groupInfoQueryDTO);
        PageResult<GroupDetailInfoVO> pageResult = PageResult.fromPage(page);
        log.info("获取到群聊信息 {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 解散群聊
     */
    @DeleteMapping("/dissolutionGroup")
    public Result<String> dissolutionGroup(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("解散群聊 groupId: {}", groupId);
        groupInfoService.dissolutionGroup(groupId);
        return Result.success();
    }
}
