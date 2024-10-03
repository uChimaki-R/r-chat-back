package com.r.chat.controller;

import com.r.chat.entity.dto.ApplyDTO;
import com.r.chat.entity.dto.ContactSearchResultDTO;
import com.r.chat.entity.dto.BasicInfoDTO;
import com.r.chat.entity.enums.JoinTypeEnum;
import com.r.chat.entity.enums.UserContactTypeEnum;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.ContactSearchResultVO;
import com.r.chat.entity.vo.BasicInfoVO;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final IUserContactService userContactService;

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
        log.info("添加用户或群聊 applyDTO: {}", applyDTO);
        return Result.success(userContactService.applyAdd(applyDTO));
    }

    /**
     * 加载好友或加入的群聊
     */
    @GetMapping("/loadContact")
    public Result<List<BasicInfoVO>> loadContact(UserContactTypeEnum userContactType) {
        log.info("查询好友/加入的群聊");
        List<BasicInfoDTO> basicInfoDTOList = userContactService.loadContact(userContactType);
        List<BasicInfoVO> basicInfoVOList = CopyUtils.copyList(basicInfoDTOList, BasicInfoVO.class);
        return Result.success(basicInfoVOList);
    }

}
