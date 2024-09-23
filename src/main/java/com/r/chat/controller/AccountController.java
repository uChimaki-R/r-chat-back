package com.r.chat.controller;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.exception.BusinessException;
import com.r.chat.redis.RedisUtils;
import com.r.chat.entity.vo.Result;
import com.r.chat.service.IUserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    private final IUserInfoService userInfoService;

    /**
     * 获取验证码图片
     */
    @GetMapping("/checkCode")
    public Result<Map<String, String>> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 30);
        String base64 = captcha.toBase64(); // 验证码图片的base64编码
        String code = captcha.text(); // 验证码的结果
        String checkCodeKey = UUID.randomUUID().toString(); // 用户提交验证码结果时的唯一标识
        log.info("CheckCode for [{}] is {}", checkCodeKey, code);
        // 保存到redis，设置10分钟的时间
        redisUtils.setEx(Constants.REDIS_KEY_CHECK_CODE_PREFIX + checkCodeKey, code, 10, TimeUnit.MINUTES);
        Map<String, String> map = new HashMap<>();
        map.put("checkCode", base64);
        map.put("checkCodeKey", checkCodeKey);
        return Result.success(map);
    }

    /**
     * 注册账号
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        // 先判断验证码是否正确
        String checkCodeKey = registerDTO.getCheckCodeKey();
        String code = redisUtils.get(Constants.REDIS_KEY_CHECK_CODE_PREFIX + checkCodeKey);
        if (code == null || !code.equals(registerDTO.getCheckCode())) {
            throw new BusinessException("验证码错误");
        }
        try {
            // 注册账号
            userInfoService.register(registerDTO);
            return Result.success();
        } finally {
            // 无论成功与否都要删除掉验证码，防止重复提交暴力破解验证码
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE_PREFIX + checkCodeKey);
        }
    }
}
