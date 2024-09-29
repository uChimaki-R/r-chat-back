package com.r.chat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "r.chat.default-settings")
@Data
public class DefaultSysSettingProperties {
    // 个人最多群组数量
    private Integer maxGroupCount;
    // 单个群组最多群成员
    private Integer maxGroupMemberCount;
    // 图片最大大小
    private Integer maxImageSize;
    // 视频最大大小
    private Integer maxVideoSize;
    // 文件最大大小
    private Integer maxFileSize;
    // 初始机器人id
    private String robotId;
    // 初始机器人昵称
    private String robotNickName;
    // 初始机器人欢迎语
    private String robotWelcomeMsg;
}
