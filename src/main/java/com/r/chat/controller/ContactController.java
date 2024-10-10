package com.r.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.*;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.entity.enums.UserContactStatusEnum;
import com.r.chat.entity.result.PageResult;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.*;
import com.r.chat.service.IUserContactApplyService;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final IUserContactService userContactService;
    private final IUserContactApplyService userContactApplyService;

    /**
     * 搜索用户或群聊
     */
    @GetMapping("/search")
    public Result<ContactSearchResultVO> search(String contactId) {
        log.info("搜索用户或群聊 contactId: {}", contactId);
        ContactSearchResultDTO contactSearchResultDTO = userContactService.search(contactId);
        ContactSearchResultVO contactSearchResultVO = CopyUtils.copyBean(contactSearchResultDTO, ContactSearchResultVO.class);
        return Result.success(contactSearchResultVO);
    }

    /**
     * 请求添加用户或群聊
     *
     * @return 返回添加类型的枚举，0为可以直接添加，1需要对方确认后才能添加
     */
    @PostMapping("/applyAdd")
    public Result<JoinTypeEnum> applyAdd(ApplyDTO applyDTO) {
        log.info("添加用户或群聊 {}", applyDTO);
        return Result.success(userContactService.applyAdd(applyDTO));
    }

    /**
     * 加载申请信息
     */
    @GetMapping("/loadApply")
    public Result<PageResult<ContactApplyVO>> loadApply(Long pageNo, Long pageSize) {
        // 加人的申请返回申请人的信息，加群申请不仅返回申请人的信息，还要返回是想加入哪个群的信息
        // 这里和下面的loadContact不同，loadContact是加载自己的好友/群聊信息，这里是加载别人申请的信息，如果自己是群主的话还会接收到加群的申请
        // 作为群主的自己，想看到的肯定是"谁"想加入我的"哪一个群聊"两个信息，所以统一一起返回
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? Constants.SIZE_DEFAULT_PAGE_SIZE : pageSize;
        log.info("加载申请信息 pageNo: {}, pageSize: {}", pageNo, pageSize);
        // 涉及用户名称/群聊名称的填充，需要多表联查
        Page<ContactApplyVO> page = userContactApplyService.getApplyInfoPage(new Page<>(pageNo, pageSize));
        // 包装成自己的pageResult对象
        PageResult<ContactApplyVO> pageResult = new PageResult<>();
        pageResult.setPageSize(pageSize);
        pageResult.setPageNo(pageNo);
        pageResult.setPageTotal(page.getPages());
        pageResult.setTotalCount(page.getTotal());
        pageResult.setData(page.getRecords());
        log.info("获取到申请信息 {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 处理申请
     */
    @PostMapping("/dealWithApply")
    public Result<String> dealWithApply(ApplyDealDTO applyDealDTO) {
        log.info("处理申请信息 {}", applyDealDTO);
        userContactApplyService.dealWithApply(applyDealDTO);
        return Result.success();
    }

    /**
     * 加载好友或加入的群聊
     */
    @GetMapping("/loadContact")
    public Result<List<BasicInfoVO>> loadContact(ContactTypeDTO contactTypeDTO) {
        log.info("查询好友/加入的群聊 {}", contactTypeDTO);
        List<BasicInfoDTO> basicInfoDTOList = userContactService.loadContact(contactTypeDTO);
        List<BasicInfoVO> basicInfoVOList = CopyUtils.copyList(basicInfoDTOList, BasicInfoVO.class);
        return Result.success(basicInfoVOList);
    }

    /**
     * 点击头像时获取的基础信息（名片，非好友也可以获取）
     */
    @GetMapping("/getContactBasicInfo")
    public Result<ContactBasicInfoVO> getContactBasicInfo(String contactId) {
        log.info("获取用户名片信息 contactId: {}", contactId);
        ContactBasicInfoDTO contactBasicInfoDTO = userContactService.getContactBasicInfo(contactId);
        ContactBasicInfoVO contactBasicInfoVO = CopyUtils.copyBean(contactBasicInfoDTO, ContactBasicInfoVO.class);
        return Result.success(contactBasicInfoVO);
    }

    /**
     * 获取联系人详情
     */
    @GetMapping("/getContactDetailInfo")
    public Result<ContactDetailInfoVO> getContactDetailInfo(String contactId) {
        log.info("获取用户详细信息 contactId: {}", contactId);
        ContactDetailInfoDTO contactBasicInfoDTO = userContactService.getContactDetailInfo(contactId);
        ContactDetailInfoVO contactDetailInfoVO = CopyUtils.copyBean(contactBasicInfoDTO, ContactDetailInfoVO.class);
        return Result.success(contactDetailInfoVO);
    }

    /**
     * 删除联系人
     */
    @PutMapping("/delContact")
    public Result<String> delContact(String contactId) {
        log.info("删除联系人 contactId: {}", contactId);
        userContactService.removeContact(contactId, UserContactStatusEnum.DELETED_THE_FRIEND);
        return Result.success();
    }

    /**
     * 拉黑联系人
     */
    @PostMapping("/blockContact")
    public Result<String> blockContact(String contactId) {
        log.info("拉黑联系人 contactId: {}", contactId);
        userContactService.removeContact(contactId, UserContactStatusEnum.BLOCKED_THE_FRIEND);
        return Result.success();
    }

}
