package com.r.chat.controller;

import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.dto.LoginDTO;
import com.r.chat.entity.dto.RegisterDTO;
import com.r.chat.entity.dto.UserInfoDTO;
import com.r.chat.entity.dto.UserTokenInfoDTO;
import com.r.chat.entity.vo.CheckCodeVO;
import com.r.chat.entity.vo.SysSettingVO;
import com.r.chat.entity.vo.UserTokenInfoVO;
import com.r.chat.exception.CheckCodeErrorException;
import com.r.chat.redis.RedisOperation;
import com.r.chat.entity.result.Result;
import com.r.chat.redis.RedisUtils;
import com.r.chat.service.IUserInfoService;
import com.r.chat.utils.CopyUtils;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final RedisOperation redisOperation;
    private final IUserInfoService userInfoService;
    private final RedisUtils redisUtils;

    /**
     * 获取验证码图片
     */
    @GetMapping("/checkCode")
    public Result<CheckCodeVO> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 30);
        String base64 = captcha.toBase64(); // 验证码图片的base64编码
        String code = captcha.text(); // 验证码的结果
        String checkCodeKey = UUID.randomUUID().toString(); // 用户提交验证码结果时的唯一标识
        log.info("获取验证码 checkCodeKey: [{}], code: {}", checkCodeKey, code);
        // 保存到redis，设置10分钟的时间
        redisOperation.setEx(Constants.REDIS_KEY_PREFIX_CHECK_CODE + checkCodeKey, code, 10, TimeUnit.MINUTES);
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(base64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return Result.success(checkCodeVO);
    }

    /**
     * 校验验证码
     *
     * @param checkCodeKey UUID唯一标识
     * @param checkCode    输入的验证码
     */
    private void checkCheckCode(String checkCodeKey, String checkCode) {
        // 获取正确的验证码
        String code = (String) redisOperation.get(Constants.REDIS_KEY_PREFIX_CHECK_CODE + checkCodeKey);
        // 无论成功与否都要删除掉验证码，防止重复提交暴力破解验证码
        redisOperation.delete(Constants.REDIS_KEY_PREFIX_CHECK_CODE + checkCodeKey);
        if (code == null || !code.equals(checkCode)) {
            log.warn("拒绝注册/登录: 验证码验证不通过: {} != {}", checkCode, code);
            throw new CheckCodeErrorException(Constants.MESSAGE_CHECK_CODE_ERROR);
        }
    }

    /**
     * 注册账号
     */
    @PostMapping("/register")
    public Result<String> register(@Valid RegisterDTO registerDTO) {
        log.info("用户注册 {}", registerDTO);
        // 先判断验证码是否正确
        checkCheckCode(registerDTO.getCheckCodeKey(), registerDTO.getCheckCode());
        // 注册账号
        userInfoService.register(registerDTO);
        return Result.success();
    }

    /**
     * 登录账号
     */
    @PostMapping("/login")
    public Result<UserTokenInfoVO> login(@Valid LoginDTO loginDTO) {
        log.info("用户登录 {}", loginDTO);
        // 先判断验证码是否正确
        checkCheckCode(loginDTO.getCheckCodeKey(), loginDTO.getCheckCode());
        // 登陆账号
        UserTokenInfoDTO userTokenInfoDTO = userInfoService.login(loginDTO);
        UserTokenInfoVO userTokenInfoVO = CopyUtils.copyBean(userTokenInfoDTO, UserTokenInfoVO.class);
        return Result.success(userTokenInfoVO);
    }

    /**
     * 获取系统设置
     */
    @GetMapping("/getSysSetting")
    public Result<SysSettingVO> getSysSetting() {
        SysSettingVO sysSettingVO = CopyUtils.copyBean(redisUtils.getSysSetting(), SysSettingVO.class);
        log.info("获取系统设置 {}", sysSettingVO);
        return Result.success(sysSettingVO);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getUserInfo")
    public Result<UserTokenInfoVO> getUserInfo() {
        UserTokenInfoVO userTokenInfoVO = CopyUtils.copyBean(UserTokenInfoContext.getCurrentUserTokenInfo(), UserTokenInfoVO.class);
        log.info("获取用户信息 {}", userTokenInfoVO);
        return Result.success(userTokenInfoVO);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/saveUserInfo")
    public Result<String> saveUserInfo(@Valid UserInfoDTO userInfoDTO) {
        log.info("更新用户信息 {}", userInfoDTO);
        userInfoService.updateUserInfo(userInfoDTO);
        return Result.success();
    }
}
