package com.r.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 个人最多群聊数量
     */
    private Integer maxGroupCount;

    /**
     * 单个群聊最多群成员
     */
    private Long maxGroupMemberCount;

    /**
     * 图片最大大小
     */
    private Integer maxImageSize;

    /**
     * 视频最大大小
     */
    private Integer maxVideoSize;

    /**
     * 文件最大大小
     */
    private Integer maxFileSize;

    /**
     * 发送文件的单次最多选择个数
     */
    private Integer maxFileCount;

    /**
     * 初始机器人id
     */
    private String robotId;

    /**
     * 初始机器人昵称
     */
    private String robotNickName;

    /**
     * 初始机器人欢迎语
     */
    private String robotWelcomeMsg;

    /**
     * 初始机器人默认回复信息
     */
    private String robotDefaultReply;
}
