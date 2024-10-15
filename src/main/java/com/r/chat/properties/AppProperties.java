package com.r.chat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "r.chat")
@Data
public class AppProperties {
    // ws端口
    private Integer wsPort;
    // 管理员邮箱
    private List<String> adminUserIds;
    // 项目文件夹
    private String projectFolder;
}
