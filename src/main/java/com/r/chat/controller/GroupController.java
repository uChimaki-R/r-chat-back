package com.r.chat.controller;

import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.BasicInfoDTO;
import com.r.chat.entity.dto.GroupMemberOpDTO;
import com.r.chat.entity.vo.GroupInfo4ChatVO;
import com.r.chat.entity.vo.GroupDetailInfoVO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.BasicInfoVO;
import com.r.chat.service.IGroupInfoService;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupInfoService groupInfoService;
    private final IUserContactService userContactService;

    /**
     * 新增或修改群聊信息
     */
    @PostMapping("/saveGroup")
    public Result<String> saveGroup(@Valid GroupInfoDTO groupInfoDTO) {
        log.info("新增或修改群聊信息 {}", groupInfoDTO);
        groupInfoService.saveOrUpdateGroupInfo(groupInfoDTO);
        return Result.success();
    }

    /**
     * 获取自己创建的群聊的信息
     */
    @GetMapping("/loadMyGroupInfo")
    public Result<List<GroupDetailInfoVO>> loadMyGroupInfo() {
        log.info("获取自己创建的群聊的信息");
        List<GroupDetailInfoVO> groupDetailInfoVOList = groupInfoService.loadMyGroupInfo();
        return Result.success(groupDetailInfoVOList);
    }

    /**
     * 获取群聊简介部分的详情
     */
    @GetMapping("/getGroupInfo")
    public Result<GroupDetailInfoVO> getGroupInfo(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("获取群聊简介部分的详情 groupId: {}", groupId);
        GroupDetailInfoVO groupDetailInfoVO = groupInfoService.getGroupDetailInfo(groupId);
        return Result.success(groupDetailInfoVO);
    }

    /**
     * 获取群聊详情，包括群成员清单
     */
    @GetMapping("getGroupInfo4Chat")
    public Result<GroupInfo4ChatVO> getGroupInfo4Chat(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("获取群聊详情(包括群成员清单) groupId: {}", groupId);
        // 群聊信息
        GroupDetailInfoVO groupDetailInfoVO = groupInfoService.getGroupDetailInfo(groupId);
        // 获取群成员信息
        List<BasicInfoDTO> basicInfoDTOList = userContactService.getGroupMemberInfo(groupId);
        List<BasicInfoVO> userContactList = CopyUtils.copyList(basicInfoDTOList, BasicInfoVO.class);
        // 包装成vo对象
        GroupInfo4ChatVO groupInfo4ChatVO = new GroupInfo4ChatVO();
        groupInfo4ChatVO.setGroupInfo(groupDetailInfoVO);
        groupInfo4ChatVO.setUserContactList(userContactList);
        log.info("获取群聊详情(包括群成员清单) {}", groupInfo4ChatVO);
        return Result.success(groupInfo4ChatVO);
    }

    /**
     * 解散群聊
     */
    @DeleteMapping("/disbandGroup")
    public Result<String> disbandGroup(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("解散群聊 groupId: {}", groupId);
        groupInfoService.disbandGroup(groupId);
        return Result.success();
    }

    /**
     * 离开群聊
     */
    @PostMapping("/leaveGroup")
    public Result<String> leaveGroup(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("离开群聊 groupId: {}", groupId);
        groupInfoService.leaveGroup(UserTokenInfoContext.getCurrentUserId(), groupId);
        log.info("已离开群聊 groupId: {}", groupId);
        return Result.success();
    }

    /**
     * 添加或移除群成员
     */
    @PostMapping("/addOrRemoveGroupMember")
    public Result<String> addOrRemoveGroupMember(@Valid GroupMemberOpDTO groupMemberOpDTO) {
        log.info("添加或移除群成员 {}", groupMemberOpDTO);
        groupInfoService.addOrRemoveGroupMember(groupMemberOpDTO);
        log.info("添加或移除群成员成功 {}", groupMemberOpDTO);
        return Result.success();
    }

}
