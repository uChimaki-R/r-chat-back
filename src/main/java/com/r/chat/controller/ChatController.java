package com.r.chat.controller;

import com.r.chat.entity.dto.ChatMessageDTO;
import com.r.chat.entity.dto.FileUploadDTO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.ChatMessageVO;
import com.r.chat.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final IChatMessageService chatMessageServiceImpl;

    /**
     * 用户发送消息
     * @return 返回增加的ChatMessage内容加上lastMessage、lastReceiveTime包装成的ChatMessageVO，让前端更新（messageId由后端产生）
     */
    @PostMapping("/sendMessage")
    public Result<ChatMessageVO> sendMessage(@Valid ChatMessageDTO chatMessageDTO) {
        log.info("发送聊天信息 {}", chatMessageDTO);
        ChatMessageVO chatMessage = chatMessageServiceImpl.saveMessage(chatMessageDTO);
        log.info("返回发送聊天信息后更新的内容 {}", chatMessage);
        return Result.success(chatMessage);
    }

    /**
     * 上传聊天文件
     */
    @PostMapping("/uploadFile")
    public Result<String> uploadFile(@Valid FileUploadDTO uploadDTO) {
        log.info("上传文件 {}", uploadDTO);
        chatMessageServiceImpl.saveFile(uploadDTO);
        log.info("保存文件成功 {}", uploadDTO);
        return Result.success();
    }
}
