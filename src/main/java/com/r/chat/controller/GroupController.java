package com.r.chat.controller;

import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.vo.Result;
import com.r.chat.service.IGroupInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupInfoService groupInfoService;

    /**
     * 新增或修改群组信息
     */
    @PostMapping("/saveGroup")
    public Result<String> saveGroup(GroupInfoDTO groupInfoDTO) {
        log.info("群组操作: {}", groupInfoDTO);
        groupInfoService.saveOrUpdateGroupInfo(groupInfoDTO);
        return Result.success();
    }
}
