package com.r.chat.controller;

import com.r.chat.context.UserIdContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.GroupInfoDTO;
import com.r.chat.entity.dto.BasicInfoDTO;
import com.r.chat.entity.enums.GroupInfoStatusEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.po.GroupInfo;
import com.r.chat.entity.po.UserContact;
import com.r.chat.entity.vo.GroupInfo4ChatVO;
import com.r.chat.entity.vo.GroupInfoVO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.BasicInfoVO;
import com.r.chat.exception.GroupDisbandException;
import com.r.chat.exception.GroupNotExistException;
import com.r.chat.exception.IllegalOperationException;
import com.r.chat.service.IGroupInfoService;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
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
    public Result<String> saveGroup(GroupInfoDTO groupInfoDTO) {
        log.info("新增或修改群聊信息 {}", groupInfoDTO);
        groupInfoService.saveOrUpdateGroupInfo(groupInfoDTO);
        return Result.success();
    }

    /**
     * 获取自己创建的群聊
     */
    @GetMapping("/loadMyGroup")
    public Result<List<GroupInfoVO>> loadMyGroup() {
        String ownerId = UserIdContext.getCurrentUserId();
        List<GroupInfo> list = groupInfoService.lambdaQuery().eq(GroupInfo::getGroupOwnerId, ownerId).list();
        List<GroupInfoVO> groupInfoVOList = CopyUtils.copyList(list, GroupInfoVO.class);
        initGroupMemberCounts(groupInfoVOList);  // 计算群成员数
        log.info("获取创建的群聊 {}", groupInfoVOList);
        return Result.success(groupInfoVOList);
    }

    /**
     * 获取群聊简介部分的详情
     */
    @GetMapping("/getGroupInfo")
    public Result<GroupInfoVO> getGroupInfo(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        GroupInfo groupInfo = getBasicGroupInfo(groupId);
        GroupInfoVO groupInfoVO = CopyUtils.copyBean(groupInfo, GroupInfoVO.class);
        initGroupMemberCounts(groupInfoVO);  // 计算群成员数
        log.info("获取群聊简介部分的详情 {}", groupInfoVO);
        return Result.success(groupInfoVO);
    }

    /**
     * 获取群聊详情，包括群成员清单
     */
    @GetMapping("getGroupInfo4Chat")
    public Result<GroupInfo4ChatVO> getGroupInfo4Chat(@NotEmpty(message = Constants.VALIDATE_EMPTY_GROUP_ID) String groupId) {
        log.info("获取群聊详情(包括群成员清单) groupId: {}", groupId);
        GroupInfo4ChatVO groupInfo4ChatVO = new GroupInfo4ChatVO();

        // 群聊信息
        GroupInfo groupInfo = getBasicGroupInfo(groupId);
        GroupInfoVO groupInfoVO = CopyUtils.copyBean(groupInfo, GroupInfoVO.class);
        initGroupMemberCounts(groupInfoVO);

        // 获取群成员信息
        List<BasicInfoDTO> basicInfoDTOList = userContactService.getGroupMemberInfo(groupId);
        List<BasicInfoVO> userContactList = CopyUtils.copyList(basicInfoDTOList, BasicInfoVO.class);

        groupInfo4ChatVO.setGroupInfo(groupInfoVO);
        groupInfo4ChatVO.setUserContactList(userContactList);
        log.info("获取群聊详情(包括群成员清单) {}", groupInfo4ChatVO);
        return Result.success(groupInfo4ChatVO);
    }

    /**
     * 给GroupInfoVO计算成员数量
     */
    private void initGroupMemberCounts(List<GroupInfoVO> groupInfoVOList) {
        groupInfoVOList.forEach(groupInfoVO -> {
            Long count = userContactService.lambdaQuery()
                    .eq(UserContact::getContactId, groupInfoVO.getGroupId())
                    .count();
            groupInfoVO.setMemberCount(count);
        });
    }

    private void initGroupMemberCounts(GroupInfoVO groupInfoVO) {
        initGroupMemberCounts(Collections.singletonList(groupInfoVO));
    }

    /**
     * 获取群聊基本信息
     */
    private GroupInfo getBasicGroupInfo(String groupId) {
        // 看该用户是否在群里
        UserContact userContact = userContactService.lambdaQuery()
                .eq(UserContact::getUserId, UserIdContext.getCurrentUserId())
                .eq(UserContact::getContactId, groupId)
                .one();
        if (userContact == null || !UserContactStatusEnum.FRIENDS.equals(userContact.getStatus())) {
            // 不在这个群或者被移除群了，属于非法操作
            throw new IllegalOperationException(Constants.MESSAGE_NOT_IN_THE_GROUP);
        }
        GroupInfo groupInfo = groupInfoService.getById(groupId);
        if (groupInfo == null) {
            // 群聊不存在
            throw new GroupNotExistException(Constants.MESSAGE_GROUP_NOT_EXIST);
        }
        if (GroupInfoStatusEnum.DISBAND.equals(groupInfo.getStatus())) {
            // 群聊已解散
            throw new GroupDisbandException(Constants.MESSAGE_GROUP_ALREADY_DISBAND);
        }
        return groupInfo;
    }
}
