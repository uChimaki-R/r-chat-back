package com.r.chat.controller;

import com.r.chat.entity.dto.ContactSearchResultDTO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.ContactSearchResultVO;
import com.r.chat.service.IUserContactService;
import com.r.chat.utils.CopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final IUserContactService userContactService;

    /**
     * 搜索用户或群组
     */
    @GetMapping("/search")
    public Result<ContactSearchResultVO> search(String contactId) {
        log.info("搜索用户或群组 contactId: {}", contactId);
        ContactSearchResultDTO contactSearchResultDTO = userContactService.search(contactId);
        ContactSearchResultVO contactSearchResultVO = CopyUtils.copyBean(contactSearchResultDTO, ContactSearchResultVO.class);
        return Result.success(contactSearchResultVO);
    }

}
