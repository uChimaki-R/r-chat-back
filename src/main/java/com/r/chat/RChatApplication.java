package com.r.chat;

import com.github.jeffreyning.mybatisplus.conf.EnableMPP;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@EnableAsync
@MapperScan(basePackages = {"com.r.chat.mapper"})
@SpringBootApplication(scanBasePackages = {"com.r.chat"})
@Order(1)
@EnableMPP
public class RChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RChatApplication.class, args);
    }
}
