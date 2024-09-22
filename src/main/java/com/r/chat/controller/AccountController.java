package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.redis.RedisUtils;
import com.r.chat.entity.vo.Result;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    // 注入redisUtils
    private final RedisUtils redisUtils;

    /**
     * 获取验证码图片
     */
    @GetMapping("checkCode")
    public Result<Map<String, String>> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 30);
        String base64 = captcha.toBase64(); // 验证码图片的base64编码
        String code = captcha.text(); // 验证码的结果
        String checkCodeKey = UUID.randomUUID().toString(); // 用户提交验证码结果时的唯一标识
        log.info("checkCode for [{}] is {}", checkCodeKey, code);
        // 保存到redis，设置10分钟的时间
        redisUtils.setEx(Constants.REDIS_KEY_CHECK_CODE, code, 10, TimeUnit.MINUTES);
        Map<String, String> map = new HashMap<>();
        map.put("checkCode", base64);
        map.put("checkCodeKey", checkCodeKey);
        return Result.success(map);
    }
}
