package com.r.chat.entity.dto;

import com.r.chat.entity.enums.JoinTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群聊id，修改信息的时候用
     */
    private String groupId;

    /**
     * 群聊名
     */
    private String groupName;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 加群类型：0：直接加入 1：管理员同意后加入
     */
    private JoinTypeEnum joinType;

    /**
     * 头像图片文件，一个压缩过一个没压缩过
     */
    private MultipartFile avatarFile;
    private MultipartFile avatarCover;

}
