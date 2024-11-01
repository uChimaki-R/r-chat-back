package com.r.chat.service;

import com.r.chat.entity.dto.ChatMessageDTO;
import com.r.chat.entity.dto.FileDownloadDTO;
import com.r.chat.entity.dto.FileUploadDTO;
import com.r.chat.entity.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.r.chat.entity.vo.ChatMessageVO;

import java.io.File;

/**
 * <p>
 * 聊天信息 服务类
 * </p>
 *
 * @author r-pocky
 * @since 2024-10-23
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 保存用户发送的信息，更新会话信息并将更新的会话信息返回给前端用于前端的数据库更新
     */
    ChatMessageVO saveMessage(ChatMessageDTO chatMessageDTO);

    /**
     * 保存上传的文件
     */
    void saveFile(FileUploadDTO uploadDTO);

    /**
     * 获取服务端保存的文件，可能是头像文件或聊天文件
     */
    File getFile(FileDownloadDTO fileInfo);
}
