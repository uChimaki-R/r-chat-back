package com.r.chat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "r.chat")
@Data
public class AppProperties {
    private Integer wsPort;
    private List<String> adminEmails;
    private String projectFolder;
}
