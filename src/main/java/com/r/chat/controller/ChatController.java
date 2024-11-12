package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.ChatMessageDTO;
import com.r.chat.entity.dto.FileDownloadDTO;
import com.r.chat.entity.dto.FileUploadDTO;
import com.r.chat.entity.result.Result;
import com.r.chat.entity.vo.ChatDataVO;
import com.r.chat.exception.FileNotExistException;
import com.r.chat.service.IChatMessageService;
import com.r.chat.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;

@Slf4j
@Validated
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final IChatMessageService chatMessageServiceImpl;

    /**
     * 用户发送消息
     *
     * @return 返回增加的ChatMessage内容加上lastMessage、lastReceiveTime包装成的ChatMessageVO，让前端更新（messageId由后端产生）
     */
    @PostMapping("/sendMessage")
    public Result<ChatDataVO> sendMessage(@Valid ChatMessageDTO chatMessageDTO) {
        log.info("发送聊天信息 {}", chatMessageDTO);
        ChatDataVO chatMessage = chatMessageServiceImpl.saveMessage(chatMessageDTO);
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

    /**
     * 下载文件。可能是头像文件或聊天文件，如果是头像的话文件名是U/G+数字，如果是聊天文件的话文件名则是messageId
     */
    @GetMapping("/downloadFile")
    public void downloadFile(HttpServletResponse response, @Valid FileDownloadDTO downloadDTO) {
        downloadDTO.setIsCover(downloadDTO.getIsCover() == null || downloadDTO.getIsCover());  // 默认获取缩略图
        log.info("下载文件 {}", downloadDTO);
        File file = chatMessageServiceImpl.getFile(downloadDTO);
        if (file == null) {
            log.warn("获取下载文件失败: 文件不存在 {}", downloadDTO);
            throw new FileNotExistException(Constants.MESSAGE_FILE_NOT_EXIST);
        }
        log.info("获取下载文件成功, 开始下载文件 {}", file);
        // 下载文件的操作
        FileUtils.downLoadFile(response, file);
        log.info("下载文件成功 {}", downloadDTO);
    }
}
