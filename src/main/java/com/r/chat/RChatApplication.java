package com.r.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.r.chat"}, exclude = DataSourceAutoConfiguration.class)
public class RChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RChatApplication.class, args);
    }
}
