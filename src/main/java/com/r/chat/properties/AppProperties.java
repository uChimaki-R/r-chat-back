package com.r.chat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "r.chat")
@Data
public class AppProperties {
    /**
     * ws端口
     */
    private Integer wsPort;

    /**
     * 管理员邮箱
     */
    private List<String> adminUserIds;

    // 下面的属性需要在工具类中使用, 所以改为静态属性, 需要使用set方法初始化

    /**
     * 项目文件夹
     */
    public static String projectFolder;

    public void setProjectFolder(String projectFolder) {
        AppProperties.projectFolder = projectFolder;
    }

    /**
     * 用户id及群聊id长度
     */
    public static Integer idLength;

    public void setIdLength(Integer idLength) {
        AppProperties.idLength = idLength;
    }

    /**
     * token中拼接的随机字符串的长度
     */
    public static Integer tokenRandomCharsLength;

    public void setTokenRandomCharsLength(Integer tokenRandomCharsLength) {
        AppProperties.tokenRandomCharsLength = tokenRandomCharsLength;
    }
}
